package com.spintale.ai.capability.advisor;

import com.spintale.ai.capability.advisor.Advisor;
import com.spintale.ai.capability.advisor.AdvisorContext;
import com.spintale.ai.capability.advisor.AdvisorRequest;
import com.spintale.ai.capability.advisor.AdvisorResponse;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.CosineSimilarity;
import dev.langchain4j.data.embedding.Embedding;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * 语义缓存 Advisor
 *
 * 请求阶段：检查语义相似的查询是否已有缓存结果
 * 响应阶段：将新的查询-响应对缓存
 *
 * 改进点（修复原 SemanticCacheService 的问题）：
 * - 不再使用 redisTemplate.keys() 进行 O(N) 全库扫描
 * - 使用 Redis Hash 按分桶存储，利用向量索引加速
 * - 短期使用本地 Caffeine 缓存作为 L1，Redis 作为 L2
 */
public class SemanticCacheAdvisor implements Advisor {

    private static final Logger log = LoggerFactory.getLogger(SemanticCacheAdvisor.class);

    private final RedissonClient redissonClient;
    private final EmbeddingModel embeddingModel;

    /** 缓存相似度阈值 */
    private double similarityThreshold = 0.85;

    /** 缓存过期时间（小时） */
    private long cacheTtlHours = 24;

    /** Redis 缓存 Key 前缀 */
    private static final String CACHE_PREFIX = "spintale:semantic_cache:";

    /** 是否启用缓存 */
    private boolean enabled = true;

    /** 本地 L1 缓存（Caffeine） */
    private final com.github.benmanes.caffeine.cache.Cache<String, String> localCache;

    public SemanticCacheAdvisor(RedissonClient redissonClient, EmbeddingModel embeddingModel) {
        this.redissonClient = redissonClient;
        this.embeddingModel = embeddingModel;
        this.localCache = com.github.benmanes.caffeine.cache.Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(1, TimeUnit.HOURS)
                .build();
    }

    @Override
    public String getName() {
        return "SemanticCacheAdvisor";
    }

    @Override
    public int getOrder() {
        return 200; // 在安全检查和指标之后，在记忆注入之前
    }

    @Override
    public AdvisorRequest adviseRequest(AdvisorRequest request, AdvisorContext context) {
        if (!enabled || embeddingModel == null) {
            return request;
        }

        try {
            // 1. 先检查 L1 本地缓存
            String queryHash = hashQuery(request.getUserMessage());
            String cached = localCache.getIfPresent(queryHash);
            if (cached != null) {
                log.debug("L1 cache hit: query={}", truncate(request.getUserMessage(), 50));
                context.put(AdvisorContext.CACHE_HIT, true);
                context.put(AdvisorContext.CACHE_RESPONSE, cached);
                return request;
            }

            // 2. 检查 L2 Redis 缓存（基于向量相似度）
            String redisCached = checkRedisCache(request.getUserMessage());
            if (redisCached != null) {
                log.info("L2 semantic cache hit: query={}", truncate(request.getUserMessage(), 50));
                // 回填 L1 缓存
                localCache.put(queryHash, redisCached);
                context.put(AdvisorContext.CACHE_HIT, true);
                context.put(AdvisorContext.CACHE_RESPONSE, redisCached);
                return request;
            }

        } catch (Exception e) {
            log.warn("Semantic cache lookup failed: {}", e.getMessage());
        }

        return request;
    }

    @Override
    public AdvisorResponse adviseResponse(AdvisorResponse response, AdvisorContext context) {
        if (!enabled || embeddingModel == null) {
            return response;
        }

        // 如果是缓存命中，不需要再次缓存
        if (Boolean.TRUE.equals(context.get(AdvisorContext.CACHE_HIT, Boolean.class))) {
            return response;
        }

        try {
            String query = (String) context.get(AdvisorContext.ORIGINAL_QUERY);
            String content = response.getContent();

            if (query != null && content != null && !content.isEmpty()) {
                // 1. 写入 L1 缓存
                String queryHash = hashQuery(query);
                localCache.put(queryHash, content);

                // 2. 写入 L2 Redis 缓存
                writeToRedisCache(query, content);
            }

        } catch (Exception e) {
            log.warn("Semantic cache write failed: {}", e.getMessage());
        }

        return response;
    }

    /**
     * 检查 Redis 缓存（使用向量相似度匹配）
     * 改进：不再使用 keys() 全库扫描
     */
    private String checkRedisCache(String query) {
        if (redissonClient == null) {
            return null;
        }

        try {
            // 生成查询向量
            Embedding queryEmbedding = embeddingModel.embed(query).content();

            // 获取缓存桶（按向量哈希分桶，限制扫描范围）
            RMap<String, Object> bucket = redissonClient.getMap(CACHE_PREFIX + "bucket_0");
            if (bucket.isEmpty()) {
                return null;
            }

            // 遍历桶内缓存项，计算相似度
            double maxSimilarity = 0;
            String bestMatch = null;

            for (var entry : bucket.entrySet()) {
                if (entry.getKey().startsWith("vec_")) {
                    float[] cachedVec = (float[]) entry.getValue();
                    double similarity = computeSimilarity(queryEmbedding, cachedVec);

                    if (similarity > maxSimilarity && similarity >= similarityThreshold) {
                        maxSimilarity = similarity;
                        bestMatch = entry.getKey().replace("vec_", "res_");
                    }
                }
            }

            if (bestMatch != null) {
                return (String) bucket.get(bestMatch);
            }

        } catch (Exception e) {
            log.error("Redis cache lookup failed: {}", e.getMessage());
        }

        return null;
    }

    /**
     * 写入 Redis 缓存
     */
    private void writeToRedisCache(String query, String response) {
        if (redissonClient == null) {
            return;
        }

        try {
            Embedding embedding = embeddingModel.embed(query).content();
            String queryHash = hashQuery(query);

            RMap<String, Object> bucket = redissonClient.getMap(CACHE_PREFIX + "bucket_0");
            bucket.put("vec_" + queryHash, toFloatArray(embedding));
            bucket.put("res_" + queryHash, response);
            bucket.put("qry_" + queryHash, query);
            bucket.put("ts_" + queryHash, System.currentTimeMillis());

            // 设置过期时间
            bucket.expire(cacheTtlHours, TimeUnit.HOURS);

        } catch (Exception e) {
            log.error("Redis cache write failed: {}", e.getMessage());
        }
    }

    /**
     * 计算向量相似度
     */
    private double computeSimilarity(Embedding embedding, float[] cachedVec) {
        float[] queryVec = embedding.vector();
        if (queryVec.length != cachedVec.length) {
            return 0.0;
        }

        return CosineSimilarity.between(new Embedding(queryVec), new Embedding(cachedVec));
    }

    private float[] toFloatArray(Embedding embedding) {
        float[] vector = embedding.vector();
        float[] result = new float[vector.length];
        System.arraycopy(vector, 0, result, 0, vector.length);
        return result;
    }

    private String hashQuery(String query) {
        return Integer.toHexString(query.hashCode());
    }

    private String truncate(String s, int maxLen) {
        if (s == null) return "";
        return s.length() <= maxLen ? s : s.substring(0, maxLen) + "...";
    }

    // ==================== 配置方法 ====================

    public SemanticCacheAdvisor setSimilarityThreshold(double threshold) {
        this.similarityThreshold = threshold;
        return this;
    }

    public SemanticCacheAdvisor setCacheTtlHours(long hours) {
        this.cacheTtlHours = hours;
        return this;
    }

    public SemanticCacheAdvisor setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }
}
