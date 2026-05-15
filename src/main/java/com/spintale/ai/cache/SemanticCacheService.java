package com.spintale.ai.cache;

import com.spintale.ai.retriever.EmbeddingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 语义缓存服务
 * 基于向量相似度拦截重复/相似问题，避免重复调用 LLM
 */
@Slf4j
@Service
public class SemanticCacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final EmbeddingService embeddingService;
    
    // 缓存配置
    private static final String CACHE_KEY_PREFIX = "semantic_cache:";
    private static final double SIMILARITY_THRESHOLD = 0.85; // 相似度阈值
    private static final long CACHE_TTL_HOURS = 24; // 缓存过期时间

    public SemanticCacheService(RedisTemplate<String, Object> redisTemplate, 
                                EmbeddingService embeddingService) {
        this.redisTemplate = redisTemplate;
        this.embeddingService = embeddingService;
    }

    /**
     * 查找语义相似的缓存结果
     * @param query 用户查询
     * @return 缓存的响应内容（如果命中）
     */
    public Optional<String> getSimilarResponse(String query) {
        try {
            // 生成查询向量
            List<Float> queryVector = embeddingService.embed(query);
            
            // 获取所有缓存键
            var keys = redisTemplate.keys(CACHE_KEY_PREFIX + "*");
            if (keys == null || keys.isEmpty()) {
                return Optional.empty();
            }

            double maxSimilarity = 0;
            String bestMatchKey = null;

            // 遍历所有缓存项，计算相似度
            for (String key : keys) {
                // 获取缓存的向量
                @SuppressWarnings("unchecked")
                List<Float> cachedVector = (List<Float>) redisTemplate.opsForHash().get(key, "vector");
                
                if (cachedVector != null) {
                    double similarity = cosineSimilarity(queryVector, cachedVector);
                    
                    if (similarity > maxSimilarity && similarity >= SIMILARITY_THRESHOLD) {
                        maxSimilarity = similarity;
                        bestMatchKey = key;
                    }
                }
            }

            if (bestMatchKey != null) {
                @SuppressWarnings("unchecked")
                String cachedResponse = (String) redisTemplate.opsForHash().get(bestMatchKey, "response");
                log.info("语义缓存命中 [相似度: {:.2f}]: {}", maxSimilarity, query);
                return Optional.ofNullable(cachedResponse);
            }

            log.debug("语义缓存未命中: {}", query);
            return Optional.empty();
            
        } catch (Exception e) {
            log.error("语义缓存查询失败", e);
            return Optional.empty();
        }
    }

    /**
     * 将查询结果存入语义缓存
     * @param query 用户查询
     * @param response LLM 响应
     */
    public void cacheResponse(String query, String response) {
        try {
            // 生成查询向量
            List<Float> queryVector = embeddingService.embed(query);
            
            // 使用查询的哈希值作为键的一部分
            String cacheKey = CACHE_KEY_PREFIX + Math.abs(query.hashCode());
            
            // 存储向量、原始查询和响应
            redisTemplate.opsForHash().put(cacheKey, "vector", queryVector);
            redisTemplate.opsForHash().put(cacheKey, "query", query);
            redisTemplate.opsForHash().put(cacheKey, "response", response);
            redisTemplate.opsForHash().put(cacheKey, "timestamp", System.currentTimeMillis());
            
            // 设置过期时间
            redisTemplate.expire(cacheKey, CACHE_TTL_HOURS, TimeUnit.HOURS);
            
            log.info("语义缓存已存储: {}", query);
            
        } catch (Exception e) {
            log.error("语义缓存存储失败", e);
        }
    }

    /**
     * 计算余弦相似度
     */
    private double cosineSimilarity(List<Float> vec1, List<Float> vec2) {
        if (vec1.size() != vec2.size()) {
            throw new IllegalArgumentException("向量维度必须相同");
        }

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (int i = 0; i < vec1.size(); i++) {
            dotProduct += vec1.get(i) * vec2.get(i);
            norm1 += vec1.get(i) * vec1.get(i);
            norm2 += vec2.get(i) * vec2.get(i);
        }

        if (norm1 == 0 || norm2 == 0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    /**
     * 清除所有语义缓存
     */
    public void clearCache() {
        var keys = redisTemplate.keys(CACHE_KEY_PREFIX + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.info("语义缓存已清空");
        }
    }

    /**
     * 获取缓存统计信息
     */
    public CacheStats getStats() {
        var keys = redisTemplate.keys(CACHE_KEY_PREFIX + "*");
        int size = (keys != null) ? keys.size() : 0;
        return new CacheStats(size, CACHE_TTL_HOURS, SIMILARITY_THRESHOLD);
    }

    public record CacheStats(int size, long ttlHours, double similarityThreshold) {}
}
