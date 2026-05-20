package com.spintale.ai.agent.memory.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.spintale.ai.agent.memory.api.ConversationManager;
import com.spintale.ai.agent.memory.ConversationSession;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class MemoryConversationManager implements ConversationManager {
    
    private final Cache<String, ConversationSession> sessions;
    
    public MemoryConversationManager(
            @Value("${spintale.ai.session.expire-minutes:30}") int expireMinutes,
            @Value("${spintale.ai.session.max-size:10000}") int maxSize) {
        
        this.sessions = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterAccess(Duration.ofMinutes(expireMinutes))
                .expireAfterWrite(Duration.ofMinutes(expireMinutes * 2))
                .removalListener((key, value, cause) -> {
                    if (value != null) {
                        ConversationSession session = (ConversationSession) value;
                    }
                })
                .build();
    }
    
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
        ConversationSession session = sessions.getIfPresent(sessionId);
        if (session != null) {
            session.setLastActiveAt(LocalDateTime.now());
        }
        return session;
    }
    
    @Override
    public void deleteSession(String sessionId) {
        sessions.invalidate(sessionId);
    }
    
    @Override
    public void addMessage(String sessionId, String role, String content) {
        ConversationSession session = sessions.getIfPresent(sessionId);
        if (session != null) {
            session.addMessage(role, content);
            session.setLastActiveAt(LocalDateTime.now());
        }
    }
    
    @Override
    public void clearSession(String sessionId) {
        ConversationSession session = sessions.getIfPresent(sessionId);
        if (session != null) {
            session.clear();
            session.setLastActiveAt(LocalDateTime.now());
        }
    }
    
    @Override
    public List<ConversationSession> listSessions() {
        Map<String, ConversationSession> sessionMap = sessions.asMap();
        return new ArrayList<>(sessionMap.values());
    }
}
