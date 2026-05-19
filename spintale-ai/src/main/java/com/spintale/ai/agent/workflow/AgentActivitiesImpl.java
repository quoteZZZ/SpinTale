package com.spintale.ai.agent.workflow;

import com.spintale.ai.agent.react.AgentResult;
import com.spintale.ai.core.service.AiChatService;
import com.spintale.ai.core.model.ChatRequest;
import com.spintale.ai.core.model.ChatResponse;
import com.spintale.ai.generation.model.GenerationRequest;
import com.spintale.ai.generation.model.GenerationResponse;
import com.spintale.ai.capability.hallucination.HallucinationDetector;
import com.spintale.ai.capability.hallucination.HallucinationReport;
import com.spintale.ai.capability.memory.LongTermMemoryManager;
import com.spintale.ai.capability.memory.LongTermMemory;
import com.spintale.ai.retrieval.vector.RetrievalResult;
import com.spintale.ai.retrieval.vector.RetrievalService;
import com.spintale.ai.tool.registry.AiTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Temporal activity implementation for agent workflow steps.
 */
public class AgentActivitiesImpl implements AgentActivities {

    private static final Logger log = LoggerFactory.getLogger(AgentActivitiesImpl.class);

    private final RetrievalService retrievalService;
    private final AiChatService chatService;
    private final Map<String, AiTool> tools;
    private final HallucinationDetector hallucinationService;
    private final LongTermMemoryManager memoryManager;

    public AgentActivitiesImpl(
            RetrievalService retrievalService,
            AiChatService chatService,
            List<AiTool> toolList,
            HallucinationDetector hallucinationService,
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
        String prompt = request == null ? "" : defaultString(request.getPrompt());
        log.info("Calling LLM with prompt length: {}", prompt.length());
        
        try {
            ChatRequest chatRequest = new ChatRequest();
            // Create ChatMessage using static factory method
            var userMessage = com.spintale.ai.core.model.ChatMessage.user(prompt);
            chatRequest.setMessages(List.of(userMessage));
            chatRequest.setTemperature(request != null && request.getTemperature() != null ? request.getTemperature().doubleValue() : 0.7);
            chatRequest.setMaxTokens(request == null ? null : request.getMaxTokens());
            
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
            String result = tool.execute(parseToolArgs(toolArgs));
            log.info("Tool execution successful: {}", toolName);
            return new AgentResult.ToolExecutionResult(toolName, result, true);
        } catch (Exception e) {
            log.error("Tool execution failed: {}", toolName, e);
            return new AgentResult.ToolExecutionResult(toolName, e.getMessage(), false);
        }
    }

    @Override
    public HallucinationReport detectHallucination(
            GenerationResponse response, 
            List<String> context) {
        log.info("Detecting hallucinations in response");
        
        if (response == null || response.getContent() == null) {
            HallucinationReport result = new HallucinationReport();
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
            return HallucinationReport.from(internalResult);
        } catch (Exception e) {
            log.error("Hallucination detection failed", e);
            HallucinationReport result = new HallucinationReport();
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
                request == null ? "" : request.getPrompt(),
                reasoningTrace,
                verifiedContext
            );
            
            ChatRequest chatRequest = new ChatRequest();
            // Create ChatMessage using static factory method
            var userMessage = com.spintale.ai.core.model.ChatMessage.user(prompt);
            chatRequest.setMessages(List.of(userMessage));
            chatRequest.setTemperature(0.5); // Lower temperature for more deterministic output
            chatRequest.setMaxTokens(request == null ? null : request.getMaxTokens());
            
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
            memory.setContent(request == null ? "" : defaultString(request.getPrompt()));
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

    private String buildFinalResponsePrompt(
            String originalQuery,
            String reasoningTrace,
            List<String> verifiedContext) {
        
        StringBuilder prompt = new StringBuilder();
        prompt.append("Based on the following reasoning process and verified context, ")
              .append("provide a clear and accurate final answer to the user's question.\n\n");
        
        prompt.append("Original Question: ").append(defaultString(originalQuery)).append("\n\n");
        
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

    private Map<String, Object> parseToolArgs(String toolArgs) {
        if (toolArgs == null || toolArgs.isBlank()) {
            return Map.of();
        }
        try {
            return com.alibaba.fastjson2.JSON.parseObject(
                    toolArgs,
                    new com.alibaba.fastjson2.TypeReference<Map<String, Object>>() {});
        } catch (Exception ignored) {
            return Map.of("args", toolArgs);
        }
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }
}
