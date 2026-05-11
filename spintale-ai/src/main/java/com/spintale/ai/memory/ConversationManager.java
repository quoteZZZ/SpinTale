package com.spintale.ai.memory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 对话会话管理器
 */
public interface ConversationManager {
    
    /**
     * 创建新会话
     */
    ConversationSession createSession(String userId);
    
    /**
     * 获取会话
     */
    ConversationSession getSession(String sessionId);
    
    /**
     * 删除会话
     */
    void deleteSession(String sessionId);
    
    /**
     * 添加消息到会话
     */
    void addMessage(String sessionId, String role, String content);
    
    /**
     * 清空会话历史
     */
    void clearSession(String sessionId);
}
