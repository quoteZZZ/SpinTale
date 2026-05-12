package com.spintale.ai.core;

import com.spintale.ai.hallucination.HallucinationDetectionService;
import com.spintale.ai.memory.ConversationManager;
import com.spintale.ai.memory.ConversationSession;
import com.spintale.ai.memory.LongTermMemory;
import com.spintale.ai.memory.LongTermMemoryManager;
import dev.langchain4j.model.chat.ChatModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 增强型 AI 聊天服务
 * 
 * 在原有基础上增加：
 * 1. 长期记忆支持 - 跨会话记住重要信息
 * 2. 幻觉检测 - 实时检测和缓解 AI 幻觉
 * 3. 上下文管理优化 - 智能选择相关上下文
 * 4. RAG 集成 - 基于检索增强生成
 */
@Service
public class EnhancedAiChatService implements AiChatService {
    
    private static final Logger log = LoggerFactory.getLogger(EnhancedAiChatService.class);
    
    private final AiChatService delegate;
    private final ConversationManager conversationManager;
    private final LongTermMemoryManager longTermMemoryManager;
    private final HallucinationDetectionService hallucinationDetectionService;
    private final ChatModel chatModel;
    
    // 配置参数
    private int maxContextMessages = 20;  // 最大上下文消息数
    private double memoryRetrievalThreshold = 0.6;  // 记忆检索相似度阈值
    private boolean hallucinationDetectionEnabled = true;  // 是否启用幻觉检测
    
    public EnhancedAiChatService(
            AiChatService delegate,
            ConversationManager conversationManager,
            LongTermMemoryManager longTermMemoryManager,
            HallucinationDetectionService hallucinationDetectionService,
            ChatModel chatModel) {
        this.delegate = delegate;
        this.conversationManager = conversationManager;
        this.longTermMemoryManager = longTermMemoryManager;
        this.hallucinationDetectionService = hallucinationDetectionService;
        this.chatModel = chatModel;
    }
    
    @Override
    public String chat(String message) {
        return delegate.chat(message);
    }
    
    @Override
    public ChatResponse chat(ChatRequest request) {
        // 1. 获取或创建会话
        ConversationSession session = getOrCreateSession(request);
        
        // 2. 检索长期记忆
        List<LongTermMemory> relevantMemories = retrieveRelevantMemories(
                session.getUserId(), request.getMessage());
        
        // 3. 增强系统提示（注入长期记忆和检索内容）
        String enhancedSystemPrompt = enhanceSystemPrompt(
                request.getSystemPrompt(), relevantMemories);
        
        // 4. 构建优化的上下文
        List<ChatMessage> optimizedHistory = buildOptimizedContext(
                session, request.getHistory(), maxContextMessages);
        
        // 5. 创建增强后的请求
        ChatRequest enhancedRequest = new ChatRequest();
        enhancedRequest.setSessionId(request.getSessionId());
        enhancedRequest.setMessage(request.getMessage());
        enhancedRequest.setSystemPrompt(enhancedSystemPrompt);
        enhancedRequest.setHistory(optimizedHistory);
        enhancedRequest.setTemperature(request.getTemperature());
        enhancedRequest.setMaxTokens(request.getMaxTokens());
        enhancedRequest.setStream(request.getStream());
        enhancedRequest.setTools(request.getTools());
        
        // 6. 调用底层服务生成回复
        ChatResponse response = delegate.chat(enhancedRequest);
        
        // 7. 幻觉检测（如果启用）
        if (hallucinationDetectionEnabled && response != null && response.getContent() != null) {
            String context = buildContextString(optimizedHistory, request.getMessage());
            HallucinationDetectionService.HallucinationResult detectionResult = 
                    hallucinationDetectionService.detectHallucination(
                            session.getUserId(), context, response.getContent());
            
            // 如果检测到幻觉，添加警告或重新生成
            if (detectionResult.getIsHallucination()) {
                log.warn("Potential hallucination detected for user {}: confidence={}", 
                        session.getUserId(), detectionResult.getOverallConfidence());
                
                // 可以选择重新生成或添加警告
                response.setContent(hallucinationDetectionService.generateResponseWithConfidence(
                        response.getContent(), detectionResult));
            }
        }
        
        // 8. 保存对话到短期记忆
        if (session != null) {
            conversationManager.addMessage(session.getSessionId(), "user", request.getMessage());
            conversationManager.addMessage(session.getSessionId(), "assistant", 
                    response != null ? response.getContent() : "[无回复]");
        }
        
        // 9. 提取并保存重要信息到长期记忆
        extractAndSaveMemories(session.getUserId(), request.getMessage(), 
                response != null ? response.getContent() : null);
        
        return response;
    }
    
    @Override
    public void streamChat(ChatRequest request, StreamHandler handler) {
        // 流式聊天的增强处理类似，但需要特殊处理流式响应
        delegate.streamChat(request, handler);
    }
    
    /**
     * 获取或创建会话
     */
    private ConversationSession getOrCreateSession(ChatRequest request) {
        if (request.getSessionId() != null) {
            ConversationSession session = conversationManager.getSession(request.getSessionId());
            if (session != null) {
                return session;
            }
        }
        
        // 创建新会话
        String userId = request.getExtraParams() != null ? 
                (String) request.getExtraParams().get("userId") : "anonymous";
        return conversationManager.createSession(userId);
    }
    
    /**
     * 检索相关的长期记忆
     */
    private List<LongTermMemory> retrieveRelevantMemories(String userId, String query) {
        if (userId == null || query == null) {
            return new ArrayList<>();
        }
        
        try {
            return longTermMemoryManager.searchMemories(
                    userId, query, 5, memoryRetrievalThreshold);
        } catch (Exception e) {
            log.error("Failed to retrieve long-term memories: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * 增强系统提示，注入长期记忆
     */
    private String enhanceSystemPrompt(String originalPrompt, List<LongTermMemory> memories) {
        StringBuilder sb = new StringBuilder();
        
        if (originalPrompt != null) {
            sb.append(originalPrompt).append("\n\n");
        }
        
        if (!memories.isEmpty()) {
            sb.append("---\n**相关背景信息**:\n");
            for (LongTermMemory memory : memories) {
                sb.append("- [").append(memory.getType()).append("] ")
                  .append(memory.getContent()).append("\n");
            }
            sb.append("---\n");
        }
        
        return sb.toString();
    }
    
    /**
     * 构建优化的上下文
     * 策略：保留最近的对话 + 最相关的历史消息
     */
    private List<ChatMessage> buildOptimizedContext(
            ConversationSession session, 
            List<ChatMessage> providedHistory,
            int maxMessages) {
        
        List<ChatMessage> result = new ArrayList<>();
        
        // 优先使用提供的历史
        if (providedHistory != null && !providedHistory.isEmpty()) {
            result.addAll(providedHistory.stream()
                    .limit(maxMessages)
                    .collect(Collectors.toList()));
            return result;
        }
        
        // 从会话中获取历史
        if (session != null) {
            List<com.spintale.ai.memory.ConversationMessage> recentMessages = 
                    session.getRecentMessages(maxMessages);
            
            for (com.spintale.ai.memory.ConversationMessage msg : recentMessages) {
                ChatMessage chatMsg = new ChatMessage();
                chatMsg.setRole(msg.getRole().equals("user") ? "user" : "assistant");
                chatMsg.setContent(msg.getContent());
                result.add(chatMsg);
            }
        }
        
        return result;
    }
    
    /**
     * 从对话中提取重要信息并保存到长期记忆
     */
    private void extractAndSaveMemories(String userId, String userMessage, String aiResponse) {
        if (userId == null || userMessage == null) {
            return;
        }
        
        try {
            // 简单规则：提取用户陈述的事实、偏好等
            // 生产环境应使用 AI 来提取
            
            // 检测用户偏好
            if (userMessage.contains("我喜欢") || userMessage.contains("我不喜欢")) {
                LongTermMemory memory = new LongTermMemory();
                memory.setUserId(userId);
                memory.setType(LongTermMemory.MemoryType.PREFERENCE);
                memory.setContent(userMessage);
                memory.setImportanceScore(0.7);
                longTermMemoryManager.addMemory(memory);
            }
            
            // 检测事实性陈述
            if (userMessage.contains("我是") || userMessage.contains("我在") || 
                userMessage.contains("我有")) {
                LongTermMemory memory = new LongTermMemory();
                memory.setUserId(userId);
                memory.setType(LongTermMemory.MemoryType.FACT);
                memory.setContent(userMessage);
                memory.setImportanceScore(0.6);
                longTermMemoryManager.addMemory(memory);
            }
            
            // 检测重要事件
            if (aiResponse != null && (aiResponse.contains("恭喜") || 
                aiResponse.contains("遗憾") || aiResponse.contains("重要"))) {
                LongTermMemory memory = new LongTermMemory();
                memory.setUserId(userId);
                memory.setType(LongTermMemory.MemoryType.EVENT);
                memory.setContent("用户：" + userMessage + "\nAI:" + aiResponse);
                memory.setImportanceScore(0.5);
                longTermMemoryManager.addMemory(memory);
            }
            
        } catch (Exception e) {
            log.error("Failed to extract and save memories: {}", e.getMessage());
        }
    }
    
    /**
     * 将对话历史转换为字符串
     */
    private String buildContextString(List<ChatMessage> history, String currentMessage) {
        StringBuilder sb = new StringBuilder();
        for (ChatMessage msg : history) {
            sb.append(msg.getRole()).append(": ").append(msg.getContent()).append("\n");
        }
        sb.append("user: ").append(currentMessage);
        return sb.toString();
    }
    
    // ==================== 配置方法 ====================
    
    public void setMaxContextMessages(int maxContextMessages) {
        this.maxContextMessages = maxContextMessages;
    }
    
    public void setMemoryRetrievalThreshold(double memoryRetrievalThreshold) {
        this.memoryRetrievalThreshold = memoryRetrievalThreshold;
    }
    
    public void setHallucinationDetectionEnabled(boolean enabled) {
        this.hallucinationDetectionEnabled = enabled;
    }
}
