package com.spintale.ai.memory;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 基于内存的对话会话管理器实现
 */
@Service
public class InMemoryConversationManager implements ConversationManager {
    
    private final Map<String, ConversationSession> sessions = new ConcurrentHashMap<>();
    
    @Override
    public ConversationSession createSession(String userId) {
        String sessionId = UUID.randomUUID().toString();
        ConversationSession session = new ConversationSession();
        session.setSessionId(sessionId);
        session.setUserId(userId);
        session.setCreatedAt(LocalDateTime.now());
        session.setLastActiveAt(LocalDateTime.now());
        
        sessions.put(sessionId, session);
        return session;
    }
    
    @Override
    public ConversationSession getSession(String sessionId) {
        return sessions.get(sessionId);
    }
    
    @Override
    public void deleteSession(String sessionId) {
        sessions.remove(sessionId);
    }
    
    @Override
    public void addMessage(String sessionId, String role, String content) {
        ConversationSession session = sessions.get(sessionId);
        if (session != null) {
            session.addMessage(role, content);
        }
    }
    
    @Override
    public void clearSession(String sessionId) {
        ConversationSession session = sessions.get(sessionId);
        if (session != null) {
            session.clear();
        }
    }
}
