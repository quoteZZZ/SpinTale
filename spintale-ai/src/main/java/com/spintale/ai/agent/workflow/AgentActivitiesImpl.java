package com.spintale.ai.agent.workflow;

import com.spintale.ai.agent.react.AgentResult;
import com.spintale.ai.core.service.AiChatService;
import com.spintale.ai.core.model.ChatRequest;
import com.spintale.ai.core.model.ChatResponse;
import com.spintale.ai.generation.model.GenerationRequest;
import com.spintale.ai.generation.model.GenerationResponse;
import com.spintale.ai.capability.hallucination.HallucinationDetectionService;
import com.spintale.ai.capability.hallucination.HallucinationDetectionResult;
import com.spintale.ai.capability.memory.LongTermMemoryManager;
import com.spintale.ai.capability.memory.LongTermMemory;
import com.spintale.ai.retrieval.embedding.RetrievalResult;
import com.spintale.ai.retrieval.embedding.RetrievalService;
import com.spintale.ai.tool.registry.AiTool;
import dev.langchain4j.data.message.UserMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Temporal 活动实现类
 * 
 * 实现 AgentActivities 接口定义的所有活动方法，
 * 为工作流提供具体的业务逻辑执行。
 */
@Component
public class AgentActivitiesImpl implements AgentActivities {

    private static final Logger log = LoggerFactory.getLogger(AgentActivitiesImpl.class);

    private final RetrievalService retrievalService;
    private final AiChatService chatService;
    private final Map<String, AiTool> tools;
    private final HallucinationDetectionService hallucinationService;
    private final LongTermMemoryManager memoryManager;

    public AgentActivitiesImpl(
            RetrievalService retrievalService,
            AiChatService chatService,
            List<AiTool> toolList,
            HallucinationDetectionService hallucinationService,
            LongTermMemoryManager memoryManager) {
        this.retrievalService = retrievalService;
        this.chatService = chatService;
        this.tools = new ConcurrentHashMap<>();
        if (toolList != null) {
            toolList.forEach(tool -> this.tools.put(tool.getName(), tool));
        }
        this.hallucinationService = hallucinationService;
        this.memoryManager = memoryManager;
    }

    @Override
    public RetrievalResult retrieveContext(String query, int topK) {
        log.info("Retrieving context for query: {}", query);
        try {
            // Use search method instead of retrieve
            var matches = retrievalService.search(query, topK);
            
            // Convert EmbeddingMatch to RetrievalResult using the constructor
            return new RetrievalResult(matches, System.currentTimeMillis());
        } catch (Exception e) {
            log.error("Failed to retrieve context", e);
            return new RetrievalResult(List.of(), 0);
        }
    }

    @Override
    public GenerationResponse callLLM(GenerationRequest request) {
        log.info("Calling LLM with prompt length: {}", request.getPrompt().length());
        
        try {
            ChatRequest chatRequest = new ChatRequest();
            // Create ChatMessage using static factory method
            var userMessage = com.spintale.ai.core.model.ChatMessage.user(request.getPrompt());
            chatRequest.setMessages(List.of(userMessage));
            chatRequest.setTemperature(request.getTemperature() != null ? request.getTemperature().doubleValue() : 0.7);
            chatRequest.setMaxTokens(request.getMaxTokens());
            
            ChatResponse response = chatService.chat(chatRequest);
            
            return GenerationResponse.builder()
                .content(response.getContent())
                .tokenUsage(response.getTokenUsage())
                .finishReason(response.getFinishReason())
                .build();
        } catch (Exception e) {
            log.error("LLM call failed", e);
            throw new RuntimeException("LLM call failed: " + e.getMessage(), e);
        }
    }

    @Override
    public AgentResult.ToolExecutionResult executeTool(String toolName, String toolArgs) {
        log.info("Executing tool: {} with args: {}", toolName, toolArgs);
        
        AiTool tool = tools.get(toolName);
        if (tool == null) {
            String errorMsg = "Unknown tool: " + toolName;
            log.error(errorMsg);
            return new AgentResult.ToolExecutionResult(toolName, errorMsg, false);
        }
        
        try {
            String result = tool.execute(Map.of("args", toolArgs));
            log.info("Tool execution successful: {}", toolName);
            return new AgentResult.ToolExecutionResult(toolName, result, true);
        } catch (Exception e) {
            log.error("Tool execution failed: {}", toolName, e);
            return new AgentResult.ToolExecutionResult(toolName, e.getMessage(), false);
        }
    }

    @Override
    public HallucinationDetectionResult detectHallucination(
            GenerationResponse response, 
            List<String> context) {
        log.info("Detecting hallucinations in response");
        
        if (response == null || response.getContent() == null) {
            HallucinationDetectionResult result = new HallucinationDetectionResult();
            result.setIsHallucination(false);
            result.setConfidenceScore(0.0);
            result.setSummary("Empty response");
            return result;
        }
        
        try {
            // Build context string from list
            String contextStr = String.join("\n", context != null ? context : List.of());
            
            // Call the actual detection service
            var internalResult = hallucinationService.detectHallucination(
                "system", // Default user ID for system-level detection
                contextStr,
                response.getContent()
            );
            
            // Convert to workflow-compatible result
            return HallucinationDetectionResult.from(internalResult);
        } catch (Exception e) {
            log.error("Hallucination detection failed", e);
            HallucinationDetectionResult result = new HallucinationDetectionResult();
            result.setIsHallucination(false);
            result.setConfidenceScore(0.5);
            result.setSummary("Detection error: " + e.getMessage());
            return result;
        }
    }

    @Override
    public GenerationResponse generateFinalResponse(
            GenerationRequest request,
            String reasoningTrace,
            List<String> verifiedContext) {
        
        log.info("Generating final response");
        
        try {
            String prompt = buildFinalResponsePrompt(
                request.getPrompt(),
                reasoningTrace,
                verifiedContext
            );
            
            ChatRequest chatRequest = new ChatRequest();
            // Create ChatMessage using static factory method
            var userMessage = com.spintale.ai.core.model.ChatMessage.user(prompt);
            chatRequest.setMessages(List.of(userMessage));
            chatRequest.setTemperature(0.5); // Lower temperature for more deterministic output
            chatRequest.setMaxTokens(request.getMaxTokens());
            
            ChatResponse response = chatService.chat(chatRequest);
            
            return GenerationResponse.builder()
                .content(response.getContent())
                .tokenUsage(response.getTokenUsage())
                .reasoningTrace(reasoningTrace)
                .retrievedContext(verifiedContext)
                .build();
        } catch (Exception e) {
            log.error("Final response generation failed", e);
            throw new RuntimeException("Final response generation failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void saveToMemory(String conversationId, String userId, 
                            GenerationRequest request, 
                            GenerationResponse response) {
        log.info("Saving conversation to memory: {}", conversationId);
        
        // Note: LongTermMemoryManager is an interface without isLongTermMemoryEnabled method
        // Memory saving is handled by the implementation
        
        try {
            // Create a LongTermMemory object and save it
            LongTermMemory memory = new LongTermMemory();
            memory.setUserId(userId);
            memory.setType(LongTermMemory.MemoryType.FACT); // Use FACT type instead of CONVERSATION
            memory.setContent(request.getPrompt());
            memory.setMetadata(Map.of(
                "conversationId", conversationId,
                "response", response != null ? response.getContent() : "",
                "timestamp", System.currentTimeMillis()
            ));
            memory.setImportanceScore(1.0);
            
            memoryManager.addMemory(memory);
            log.info("Conversation saved to memory successfully");
        } catch (Exception e) {
            log.error("Failed to save to memory", e);
            // Don't fail the activity, just log the error
        }
    }

    /**
     * 构建最终响应生成的 Prompt
     */
    private String buildFinalResponsePrompt(
            String originalQuery,
            String reasoningTrace,
            List<String> verifiedContext) {
        
        StringBuilder prompt = new StringBuilder();
        prompt.append("Based on the following reasoning process and verified context, ")
              .append("provide a clear and accurate final answer to the user's question.\n\n");
        
        prompt.append("Original Question: ").append(originalQuery).append("\n\n");
        
        if (reasoningTrace != null && !reasoningTrace.isEmpty()) {
            prompt.append("Reasoning Process:\n").append(reasoningTrace).append("\n\n");
        }
        
        if (verifiedContext != null && !verifiedContext.isEmpty()) {
            prompt.append("Verified Context:\n");
            for (int i = 0; i < verifiedContext.size(); i++) {
                prompt.append(String.format("[%d] %s\n", i + 1, verifiedContext.get(i)));
            }
            prompt.append("\n");
        }
        
        prompt.append("Final Answer:");
        
        return prompt.toString();
    }
}
