package com.spintale.ai.core.service;

import com.spintale.ai.capability.advisor.AdvisorChain;
import com.spintale.ai.capability.advisor.AdvisorRequest;
import com.spintale.ai.capability.advisor.AdvisorResponse;
import com.spintale.ai.core.model.ChatRequest;
import com.spintale.ai.core.model.ChatResponse;
import com.spintale.ai.core.model.StreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * 基于 Advisor 链的聊天服务（门面模式）
 *
 * 整合了所有 AI 功能增强：
 * - 安全围栏
 * - 可观测性（指标采集）
 * - 语义缓存
 * - 记忆增强（长期记忆 + 上下文管理）
 * - RAG 检索增强
 * - 幻觉检测
 *
 * 使用方式：
 * <pre>
 * &#64;Autowired
 * private AdvisorChainChatService chatService;
 *
 * // 简单聊天
 * String reply = chatService.chat("user123", "你好");
 *
 * // 带上下文的聊天
 * ChatResponse response = chatService.chat(ChatRequest.builder()
 *     .sessionId("session-1")
 *     .message("帮我分析一下这个问题")
 *     .userId("user123")
 *     .build());
 * </pre>
 *
 * 改进点（对比原 EnhancedAiChatService）：
 * 1. 基于 Advisor 链模式，功能可插拔
 * 2. 不再硬编码记忆提取逻辑
 * 3. 安全检查和幻觉检测集成在链中
 * 4. 指标采集自动集成
 * 5. 语义缓存自动集成
 */
@Service
public class AdvisorChainChatService implements AiChatService {

    private static final Logger log = LoggerFactory.getLogger(AdvisorChainChatService.class);

    private final AdvisorChain advisorChain;

    public AdvisorChainChatService(AdvisorChain advisorChain) {
        this.advisorChain = advisorChain;
    }

    /**
     * 简单聊天
     */
    @Override
    public String chat(String message) {
        AdvisorRequest request = new AdvisorRequest();
        request.setUserMessage(message);
        request.setTemperature(0.7);
        request.setMaxTokens(2048);

        AdvisorResponse response = advisorChain.execute(request);
        return response.getContent();
    }

    /**
     * 带上下文的聊天
     */
    @Override
    public ChatResponse chat(ChatRequest request) {
        // 转换为 AdvisorRequest
        AdvisorRequest advisorRequest = convertToAdvisorRequest(request);

        // 执行 Advisor 链
        AdvisorResponse advisorResponse = advisorChain.execute(advisorRequest);

        // 转换为 ChatResponse
        return convertToChatResponse(advisorResponse, request.getSessionId());
    }

    /**
     * 流式聊天
     * 当前通过 AdvisorChain 不直接支持真正的流式
     * 如需真正的 Token 级别流式，请使用 StreamingChatModel 直接调用
     */
    @Override
    public void streamChat(ChatRequest request, StreamHandler handler) {
        // 降级为同步调用
        try {
            ChatResponse response = chat(request);
            if (response != null && response.getContent() != null) {
                handler.onToken(response.getContent());
                handler.onComplete(response);
            } else {
                handler.onError(new RuntimeException("No response generated"));
            }
        } catch (Exception e) {
            handler.onError(e);
        }
    }

    // ==================== 转换方法 ====================

    private AdvisorRequest convertToAdvisorRequest(ChatRequest request) {
        AdvisorRequest advisorRequest = new AdvisorRequest();
        advisorRequest.setUserMessage(request.getMessage());
        advisorRequest.setSystemPrompt(request.getSystemPrompt());
        advisorRequest.setHistory(request.getHistory());
        advisorRequest.setSessionId(request.getSessionId());
        advisorRequest.setTemperature(request.getTemperature());
        advisorRequest.setMaxTokens(request.getMaxTokens());
        advisorRequest.setStream(request.getStream());

        // 从 extraParams 提取 userId
        if (request.getExtraParams() != null) {
            Object userId = request.getExtraParams().get("userId");
            if (userId != null) {
                advisorRequest.setUserId(userId.toString());
            }
        }

        return advisorRequest;
    }

    private ChatResponse convertToChatResponse(AdvisorResponse advisorResponse, String sessionId) {
        ChatResponse response = new ChatResponse();
        response.setSessionId(sessionId);
        response.setContent(advisorResponse.getContent());
        response.setModel(advisorResponse.getModel());
        response.setTokenUsage(advisorResponse.getTokenUsage());
        response.setFinished(advisorResponse.isFinished());

        // 传递置信度信息
        if (advisorResponse.getConfidenceScore() != null) {
            Map<String, Object> extraData = new HashMap<>();
            extraData.put("confidenceScore", advisorResponse.getConfidenceScore());
            extraData.put("durationMs", advisorResponse.getMetadata("execution_duration_ms") != null
                    ? advisorResponse.getMetadata("execution_duration_ms") : 0);
            response.setExtraData(extraData);
        }

        return response;
    }
}
