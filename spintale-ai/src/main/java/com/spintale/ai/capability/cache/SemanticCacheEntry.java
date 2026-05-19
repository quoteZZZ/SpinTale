package com.spintale.ai.capability.cache;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 语义缓存条目
 * 
 * 存储向量化后的查询和对应的响应，支持相似度匹配
 */
@Data
@Slf4j
public class SemanticCacheEntry {
    
    /**
     * 缓存键（原始查询的哈希）
     */
    private String cacheKey;
    
    /**
     * 原始查询文本
     */
    private String query;
    
    /**
     * 查询的向量表示
     */
    private float[] embedding;
    
    /**
     * 缓存的响应内容
     */
    private String response;
    
    /**
     * 元数据（模型、Token使用等）
     */
    private Map<String, Object> metadata;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 最后访问时间
     */
    private LocalDateTime lastAccessedAt;
    
    /**
     * 访问次数
     */
    private int accessCount = 0;
    
    /**
     * TTL（秒）
     */
    private long ttlSeconds;
    
    public SemanticCacheEntry() {
        this.createdAt = LocalDateTime.now();
        this.lastAccessedAt = LocalDateTime.now();
    }
    
    public SemanticCacheEntry(String cacheKey, String query, float[] embedding, 
                              String response, long ttlSeconds) {
        this.cacheKey = cacheKey;
        this.query = query;
        this.embedding = embedding;
        this.response = response;
        this.createdAt = LocalDateTime.now();
        this.lastAccessedAt = LocalDateTime.now();
        this.ttlSeconds = ttlSeconds;
    }
    
    /**
     * 检查是否过期
     */
    public boolean isExpired() {
        if (ttlSeconds <= 0) {
            return false; // 永不过期
        }
        LocalDateTime expiryTime = createdAt.plusSeconds(ttlSeconds);
        return LocalDateTime.now().isAfter(expiryTime);
    }
    
    /**
     * 记录访问
     */
    public void recordAccess() {
        this.lastAccessedAt = LocalDateTime.now();
        this.accessCount++;
    }
    
    /**
     * 计算剩余存活时间（秒）
     */
    public long getRemainingTtl() {
        if (ttlSeconds <= 0) {
            return Long.MAX_VALUE;
        }
        LocalDateTime expiryTime = createdAt.plusSeconds(ttlSeconds);
        return java.time.Duration.between(LocalDateTime.now(), expiryTime).getSeconds();
    }
}
