package com.spintale.ai.workflow;

import com.spintale.ai.agent.AgentResult;
import com.spintale.ai.core.AiChatService;
import com.spintale.ai.core.ChatRequest;
import com.spintale.ai.core.ChatResponse;
import com.spintale.ai.generation.model.GenerationRequest;
import com.spintale.ai.generation.model.GenerationResponse;
import com.spintale.ai.hallucination.HallucinationDetectionService;
import com.spintale.ai.hallucination.HallucinationDetectionResult;
import com.spintale.ai.memory.LongTermMemoryManager;
import com.spintale.ai.retrieval.RetrievalResult;
import com.spintale.ai.retrieval.RetrievalService;
import com.spintale.ai.tool.AiTool;
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
            return retrievalService.retrieve(query, topK);
        } catch (Exception e) {
            log.error("Failed to retrieve context", e);
            return new RetrievalResult(List.of(), 0);
        }
    }

    @Override
    public GenerationResponse callLLM(GenerationRequest request) {
        log.info("Calling LLM with prompt length: {}", request.getPrompt().length());
        
        try {
            ChatRequest chatRequest = ChatRequest.builder()
                .messages(List.of(UserMessage.from(request.getPrompt())))
                .temperature(request.getTemperature())
                .maxTokens(request.getMaxTokens())
                .build();
            
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
            String result = tool.execute(toolArgs);
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
            return new HallucinationDetectionResult(false, 0.0, "Empty response");
        }
        
        try {
            return hallucinationService.detect(response.getContent(), context);
        } catch (Exception e) {
            log.error("Hallucination detection failed", e);
            return new HallucinationDetectionResult(false, 0.5, "Detection error: " + e.getMessage());
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
            
            ChatRequest chatRequest = ChatRequest.builder()
                .messages(List.of(UserMessage.from(prompt)))
                .temperature(0.5f) // Lower temperature for more deterministic output
                .maxTokens(request.getMaxTokens())
                .build();
            
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
        
        if (!memoryManager.isLongTermMemoryEnabled()) {
            log.debug("Long-term memory is disabled, skipping save");
            return;
        }
        
        try {
            memoryManager.saveConversation(
                conversationId,
                userId,
                request.getPrompt(),
                response != null ? response.getContent() : null
            );
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
