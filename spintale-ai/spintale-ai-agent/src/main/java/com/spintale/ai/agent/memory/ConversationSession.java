package com.spintale.ai.agent.memory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ConversationSession
{
    private String sessionId;
    private String userId;
    private List<ConversationMessage> messages = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime lastActiveAt;
    private Object metadata;

    public void addMessage(String role, String content)
    {
        ConversationMessage message = new ConversationMessage();
        message.setRole(role);
        message.setContent(content);
        message.setTimestamp(LocalDateTime.now());
        messages.add(message);
        lastActiveAt = LocalDateTime.now();
    }

    public List<ConversationMessage> getRecentMessages(int limit)
    {
        int size = messages.size();
        return size <= limit ? messages : messages.subList(size - limit, size);
    }

    public void clear()
    {
        messages.clear();
    }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public List<ConversationMessage> getMessages() { return messages; }
    public void setMessages(List<ConversationMessage> messages) { this.messages = messages; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getLastActiveAt() { return lastActiveAt; }
    public void setLastActiveAt(LocalDateTime lastActiveAt) { this.lastActiveAt = lastActiveAt; }
    public Object getMetadata() { return metadata; }
    public void setMetadata(Object metadata) { this.metadata = metadata; }
}
