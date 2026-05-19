package com.spintale.ai.capability.cache;

import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 语义缓存管理器
 * 
 * 基于向量相似度的智能缓存系统：
 * - 支持语义相似度匹配（而非精确匹配）
 * - 可配置的相似度阈值
 * - 自动过期清理
 * - LRU淘汰策略
 * 
 * 使用示例：
 * <pre>{@code
 * SemanticCache cache = new SemanticCache(embeddingModel, 0.95, 3600);
 * 
 * // 查找缓存
 * Optional<String> cached = cache.lookup("如何学习Java？");
 * 
 * // 存入缓存
 * cache.put("如何学习Java？", embedding, "从基础语法开始...");
 * }</pre>
 */
@Slf4j
public class SemanticCache {
    
    private final Map<String, SemanticCacheEntry> cacheStore;
    private final EmbeddingService embeddingService;
    private final double similarityThreshold;
    private final long defaultTtlSeconds;
    private final int maxCapacity;
    
    public SemanticCache(EmbeddingService embeddingService) {
        this(embeddingService, 0.95, 3600, 10000);
    }
    
    public SemanticCache(EmbeddingService embeddingService, double similarityThreshold, 
                        long defaultTtlSeconds, int maxCapacity) {
        this.cacheStore = new ConcurrentHashMap<>();
        this.embeddingService = embeddingService;
        this.similarityThreshold = similarityThreshold;
        this.defaultTtlSeconds = defaultTtlSeconds;
        this.maxCapacity = maxCapacity;
        
        log.info("SemanticCache initialized: threshold={}, ttl={}s, capacity={}", 
                similarityThreshold, defaultTtlSeconds, maxCapacity);
    }
    
    /**
     * 查找缓存（基于语义相似度）
     *
     * @param query 查询文本
     * @return 缓存的响应（如果找到且相似度足够高）
     */
    public Optional<CacheHit> lookup(String query) {
        if (query == null || query.isEmpty()) {
            return Optional.empty();
        }
        
        try {
            // 1. 生成查询向量
            float[] queryEmbedding = embeddingService.embed(query);
            
            // 2. 计算与所有缓存条目的相似度
            List<CacheCandidate> candidates = new ArrayList<>();
            
            for (Map.Entry<String, SemanticCacheEntry> entry : cacheStore.entrySet()) {
                SemanticCacheEntry cacheEntry = entry.getValue();
                
                // 跳过过期的条目
                if (cacheEntry.isExpired()) {
                    continue;
                }
                
                // 计算余弦相似度
                double similarity = cosineSimilarity(queryEmbedding, cacheEntry.getEmbedding());
                
                if (similarity >= similarityThreshold) {
                    candidates.add(new CacheCandidate(entry.getKey(), cacheEntry, similarity));
                }
            }
            
            // 3. 返回最相似的結果
            if (candidates.isEmpty()) {
                return Optional.empty();
            }
            
            CacheCandidate bestMatch = candidates.stream()
                    .max(Comparator.comparingDouble(c -> c.similarity()))
                    .orElse(null);
            
            if (bestMatch != null) {
                bestMatch.entry().recordAccess();
                log.debug("Cache hit: query='{}', similarity={:.3f}", query, bestMatch.similarity());
                
                return Optional.of(new CacheHit(
                        bestMatch.entry().getResponse(),
                        bestMatch.similarity(),
                        bestMatch.entry().getMetadata()
                ));
            }
            
            return Optional.empty();
            
        } catch (Exception e) {
            log.error("Cache lookup failed", e);
            return Optional.empty();
        }
    }
    
    /**
     * 存入缓存
     *
     * @param query 查询文本
     * @param response 响应内容
     * @param metadata 元数据
     */
    public void put(String query, String response, Map<String, Object> metadata) {
        if (query == null || response == null) {
            return;
        }
        
        try {
            // 检查容量
            if (cacheStore.size() >= maxCapacity) {
                evictLRU();
            }
            
            // 生成向量
            float[] embedding = embeddingService.embed(query);
            
            // 生成缓存键
            String cacheKey = generateCacheKey(query);
            
            // 创建缓存条目
            SemanticCacheEntry entry = new SemanticCacheEntry(
                    cacheKey, query, embedding, response, defaultTtlSeconds
            );
            entry.setMetadata(metadata);
            
            // 存入缓存
            cacheStore.put(cacheKey, entry);
            
            log.debug("Cache stored: key={}, size={}", cacheKey, cacheStore.size());
            
        } catch (Exception e) {
            log.error("Failed to store in cache", e);
        }
    }
    
    /**
     * 清除过期条目
     */
    public void cleanup() {
        int removed = 0;
        Iterator<Map.Entry<String, SemanticCacheEntry>> iterator = cacheStore.entrySet().iterator();
        
        while (iterator.hasNext()) {
            SemanticCacheEntry entry = iterator.next().getValue();
            if (entry.isExpired()) {
                iterator.remove();
                removed++;
            }
        }
        
        if (removed > 0) {
            log.info("Cleaned up {} expired cache entries", removed);
        }
    }
    
    /**
     * 清空缓存
     */
    public void clear() {
        cacheStore.clear();
        log.info("Cache cleared");
    }
    
    /**
     * 获取缓存统计信息
     */
    public CacheStats getStats() {
        int totalEntries = cacheStore.size();
        int expiredEntries = 0;
        long totalAccessCount = 0;
        
        for (SemanticCacheEntry entry : cacheStore.values()) {
            if (entry.isExpired()) {
                expiredEntries++;
            }
            totalAccessCount += entry.getAccessCount();
        }
        
        return new CacheStats(totalEntries, expiredEntries, totalAccessCount);
    }
    
    /**
     * 计算余弦相似度
     */
    private double cosineSimilarity(float[] vec1, float[] vec2) {
        if (vec1.length != vec2.length) {
            throw new IllegalArgumentException("Vector dimensions must match");
        }
        
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;
        
        for (int i = 0; i < vec1.length; i++) {
            dotProduct += vec1[i] * vec2[i];
            norm1 += vec1[i] * vec1[i];
            norm2 += vec2[i] * vec2[i];
        }
        
        if (norm1 == 0.0 || norm2 == 0.0) {
            return 0.0;
        }
        
        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }
    
    /**
     * 生成缓存键
     */
    private String generateCacheKey(String query) {
        return Integer.toHexString(Objects.hash(query));
    }
    
    /**
     * LRU淘汰：移除最少访问的条目
     */
    private void evictLRU() {
        Optional<Map.Entry<String, SemanticCacheEntry>> lruEntry = cacheStore.entrySet().stream()
                .min(Comparator.comparingInt(e -> e.getValue().getAccessCount()));
        
        lruEntry.ifPresent(entry -> {
            cacheStore.remove(entry.getKey());
            log.debug("Evicted LRU entry: {}", entry.getKey());
        });
    }
    
    /**
     * 缓存命中结果
     */
    public record CacheHit(String response, double similarity, Map<String, Object> metadata) {}
    
    /**
     * 缓存候选项
     */
    private record CacheCandidate(String key, SemanticCacheEntry entry, double similarity) {}
    
    /**
     * 缓存统计
     */
    public record CacheStats(int totalEntries, int expiredEntries, long totalAccessCount) {}
    
    /**
     * 嵌入服务接口
     */
    public interface EmbeddingService {
        float[] embed(String text);
    }
}
