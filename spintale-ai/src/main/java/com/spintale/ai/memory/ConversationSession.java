package com.spintale.ai.memory;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 对话会话
 */
@Data
public class ConversationSession {
    
    /**
     * 会话 ID
     */
    private String sessionId;
    
    /**
     * 用户 ID
     */
    private String userId;
    
    /**
     * 对话历史
     */
    private List<ConversationMessage> messages = new ArrayList<>();
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 最后活跃时间
     */
    private LocalDateTime lastActiveAt;
    
    /**
     * 元数据
     */
    private Object metadata;
    
    /**
     * 添加消息
     */
    public void addMessage(String role, String content) {
        ConversationMessage message = ConversationMessage.builder()
                .role(role)
                .content(content)
                .timestamp(LocalDateTime.now())
                .build();
        this.messages.add(message);
        this.lastActiveAt = LocalDateTime.now();
    }
    
    /**
     * 获取最近 N 条消息
     */
    public List<ConversationMessage> getRecentMessages(int limit) {
        int size = messages.size();
        if (size <= limit) {
            return messages;
        }
        return messages.subList(size - limit, size);
    }
    
    /**
     * 清空历史
     */
    public void clear() {
        this.messages.clear();
    }
}
