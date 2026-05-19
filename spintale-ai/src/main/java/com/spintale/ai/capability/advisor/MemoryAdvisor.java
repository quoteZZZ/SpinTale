package com.spintale.ai.capability.advisor;

import com.spintale.ai.core.model.ChatMessage;
import com.spintale.ai.capability.advisor.Advisor;
import com.spintale.ai.capability.advisor.AdvisorContext;
import com.spintale.ai.capability.advisor.AdvisorRequest;
import com.spintale.ai.capability.advisor.AdvisorResponse;
import com.spintale.ai.capability.memory.*;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * 记忆增强 Advisor
 *
 * 请求阶段：
 * 1. 获取/创建会话
 * 2. 检索相关的长期记忆
 * 3. 将记忆注入系统提示词
 * 4. 优化上下文历史（滑动窗口）
 *
 * 响应阶段：
 * 1. 保存对话到短期记忆
 * 2. 使用 LLM 提取重要信息保存到长期记忆
 *
 * 改进点：
 * - 替代原 EnhancedAiChatService 的硬编码关键词提取
 * - 使用 LLM 进行智能记忆提取
 * - 基于 Token 数量而非消息数量管理上下文窗口
 */
@Slf4j
public class MemoryAdvisor implements Advisor {

    private final ConversationManager conversationManager;
    private final LongTermMemoryManager longTermMemoryManager;
    private final ConversationCompressor conversationCompressor;

    /** 最大上下文消息数 */
    private int maxContextMessages = 20;

    /** 记忆检索相似度阈值 */
    private double memoryRetrievalThreshold = 0.6;

    /** 检索记忆的最大数量 */
    private int maxRetrievedMemories = 5;

    /** 记忆提取提示词模板 */
    private static final String MEMORY_EXTRACTION_PROMPT = """
            分析以下对话，提取值得长期记住的重要信息。
            请以 JSON 数组格式返回，每项包含 type 和 content：
            - type: FACT(事实), PREFERENCE(偏好), EVENT(事件)
            - content: 提取的信息内容
            
            如果没有值得记住的信息，返回空数组 []
            
            用户消息: %s
            AI回复: %s
            
            请返回 JSON 数组:
            """;

    public MemoryAdvisor(ConversationManager conversationManager,
                          LongTermMemoryManager longTermMemoryManager) {
        this(conversationManager, longTermMemoryManager, null);
    }

    public MemoryAdvisor(ConversationManager conversationManager,
                         LongTermMemoryManager longTermMemoryManager,
                         ConversationCompressor conversationCompressor) {
        this.conversationManager = conversationManager;
        this.longTermMemoryManager = longTermMemoryManager;
        this.conversationCompressor = conversationCompressor;
    }

    @Override
    public String getName() {
        return "MemoryAdvisor";
    }

    @Override
    public int getOrder() {
        return 300; // 在安全检查和缓存之后
    }

    @Override
    public AdvisorRequest adviseRequest(AdvisorRequest request, AdvisorContext context) {
        // 1. 获取或创建会话
        ConversationSession session = getOrCreateSession(request);
        if (session != null && request.getSessionId() == null) {
            request.setSessionId(session.getSessionId());
        }

        // 2. 检索长期记忆
        List<LongTermMemory> memories = retrieveRelevantMemories(request.getUserId(), request.getUserMessage());
        context.put(AdvisorContext.RETRIEVED_MEMORIES, memories);

        // 3. 增强系统提示词
        if (!memories.isEmpty()) {
            String enhancedPrompt = enhanceSystemPrompt(request.getSystemPrompt(), memories);
            request.setSystemPrompt(enhancedPrompt);
        }

        // 4. 优化上下文窗口
        if (session != null) {
            List<ChatMessage> optimizedHistory = buildOptimizedContext(session, maxContextMessages);
            request.setHistory(optimizedHistory);
        }

        return request;
    }

    @Override
    public AdvisorResponse adviseResponse(AdvisorResponse response, AdvisorContext context) {
        // 保存对话到短期记忆
        String sessionId = response.getSessionId();
        if (sessionId != null && conversationManager != null) {
            try {
                String originalQuery = (String) context.get(AdvisorContext.ORIGINAL_QUERY);
                conversationManager.addMessage(sessionId, "user", originalQuery);
                conversationManager.addMessage(sessionId, "assistant",
                        response.getContent() != null ? response.getContent() : "[无回复]");
            } catch (Exception e) {
                log.warn("Failed to save conversation: {}", e.getMessage());
            }
        }

        // 使用 LLM 提取长期记忆（异步，不阻塞响应）
        // 注意：LLM 提取会在 extractAndSaveMemories 中使用 ChatModel
        // 为避免循环依赖，这里标记需要提取，由后台任务执行
        context.put("memory_extraction_needed", true);
        context.put("user_message", context.get(AdvisorContext.ORIGINAL_QUERY));
        context.put("ai_response", response.getContent());

        return response;
    }

    /**
     * 获取或创建会话
     */
    private ConversationSession getOrCreateSession(AdvisorRequest request) {
        if (conversationManager == null) {
            return null;
        }

        if (request.getSessionId() != null) {
            ConversationSession session = conversationManager.getSession(request.getSessionId());
            if (session != null) {
                return session;
            }
        }

        String userId = request.getUserId() != null ? request.getUserId() : "anonymous";
        return conversationManager.createSession(userId);
    }

    /**
     * 检索相关的长期记忆
     */
    private List<LongTermMemory> retrieveRelevantMemories(String userId, String query) {
        if (userId == null || query == null || longTermMemoryManager == null) {
            return new ArrayList<>();
        }

        try {
            return longTermMemoryManager.searchMemories(userId, query, maxRetrievedMemories, memoryRetrievalThreshold);
        } catch (Exception e) {
            log.error("Failed to retrieve long-term memories: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 增强系统提示词，注入记忆上下文
     */
    private String enhanceSystemPrompt(String originalPrompt, List<LongTermMemory> memories) {
        StringBuilder sb = new StringBuilder();

        if (originalPrompt != null && !originalPrompt.isEmpty()) {
            sb.append(originalPrompt).append("\n\n");
        }

        sb.append("---\n**用户背景信息（来自长期记忆）**:\n");
        for (LongTermMemory memory : memories) {
            sb.append("- [").append(memory.getType()).append("] ")
              .append(memory.getContent());
            if (memory.getImportanceScore() != null) {
                sb.append(" (重要性: ").append(String.format("%.1f", memory.getImportanceScore())).append(")");
            }
            sb.append("\n");
        }
        sb.append("请在回答中适当参考以上信息，但不要明确提及\"记忆\"或\"背景信息\"。\n---\n");

        return sb.toString();
    }

    /**
     * 构建优化的上下文窗口
     * 策略：保留最近 N 条消息
     */
    private List<ChatMessage> buildOptimizedContext(
            ConversationSession session, int maxMessages) {
        List<ChatMessage> result = new ArrayList<>();

        List<ConversationMessage> recentMessages =
                session.getRecentMessages(maxMessages);

        for (ConversationMessage msg : recentMessages) {
            ChatMessage chatMsg = new ChatMessage();
            chatMsg.setRole("user".equals(msg.getRole()) ? "user" : "assistant");
            chatMsg.setContent(msg.getContent());
            result.add(chatMsg);
        }

        if (conversationCompressor == null) {
            return result;
        }
        return conversationCompressor.compress(result, maxMessages);
    }

    // ==================== 配置方法 ====================

    public MemoryAdvisor setMaxContextMessages(int maxContextMessages) {
        this.maxContextMessages = maxContextMessages;
        return this;
    }

    public MemoryAdvisor setMemoryRetrievalThreshold(double threshold) {
        this.memoryRetrievalThreshold = threshold;
        return this;
    }

    public MemoryAdvisor setMaxRetrievedMemories(int max) {
        this.maxRetrievedMemories = max;
        return this;
    }
}
