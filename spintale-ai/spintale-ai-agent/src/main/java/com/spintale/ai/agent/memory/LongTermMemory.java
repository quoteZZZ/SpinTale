package com.spintale.ai.agent.memory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 长期记忆实体
 * 用于存储需要持久化的重要对话内容和事实信息
 */
public class LongTermMemory {
    
    /**
     * 记忆 ID
     */
    private String id;
    
    /**
     * 用户 ID
     */
    private String userId;
    
    /**
     * 会话 ID（可选，用于关联原始对话）
     */
    private String sessionId;
    
    /**
     * 记忆内容
     */
    private String content;
    
    /**
     * 记忆类型：FACT(事实), EVENT(事件), PREFERENCE(偏好), SUMMARY(摘要)
     */
    private MemoryType type;
    
    /**
     * 重要性评分 (0.0 - 1.0)
     */
    private Double importanceScore;
    
    /**
     * 访问次数
     */
    private Integer accessCount;
    
    /**
     * 最后访问时间
     */
    private LocalDateTime lastAccessedAt;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 过期时间（可选，null 表示永不过期）
     */
    private LocalDateTime expiresAt;
    
    /**
     * 元数据（结构化数据）
     */
    private Map<String, Object> metadata;
    
    public enum MemoryType {
        FACT,       // 事实性信息
        EVENT,      // 事件记录
        PREFERENCE, // 用户偏好
        SUMMARY,    // 对话摘要
        KNOWLEDGE   // 知识库内容
    }
    
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public MemoryType getType() { return type; }
    public void setType(MemoryType type) { this.type = type; }
    public Double getImportanceScore() { return importanceScore; }
    public void setImportanceScore(Double importanceScore) { this.importanceScore = importanceScore; }
    public Integer getAccessCount() { return accessCount; }
    public void setAccessCount(Integer accessCount) { this.accessCount = accessCount; }
    public LocalDateTime getLastAccessedAt() { return lastAccessedAt; }
    public void setLastAccessedAt(LocalDateTime lastAccessedAt) { this.lastAccessedAt = lastAccessedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
}
