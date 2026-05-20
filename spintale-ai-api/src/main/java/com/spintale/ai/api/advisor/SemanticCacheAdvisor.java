package com.spintale.ai.api.advisor;

import com.spintale.ai.api.advisor.Advisor;
import com.spintale.ai.api.advisor.AdvisorContext;
import com.spintale.ai.api.advisor.AdvisorRequest;
import com.spintale.ai.api.advisor.AdvisorResponse;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.CosineSimilarity;
import dev.langchain4j.data.embedding.Embedding;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * 璇箟缂撳瓨 Advisor
 *
 * 璇锋眰闃舵锛氭鏌ヨ涔夌浉浼肩殑鏌ヨ鏄惁宸叉湁缂撳瓨缁撴灉
 * 鍝嶅簲闃舵锛氬皢鏂扮殑鏌ヨ-鍝嶅簲瀵圭紦瀛?
 *
 * 鏀硅繘鐐癸紙淇鍘?SemanticCacheService 鐨勯棶棰橈級锛?
 * - 涓嶅啀浣跨敤 redisTemplate.keys() 杩涜 O(N) 鍏ㄥ簱鎵弿
 * - 浣跨敤 Redis Hash 鎸夊垎妗跺瓨鍌紝鍒╃敤鍚戦噺绱㈠紩鍔犻??
 * - 鐭湡浣跨敤鏈湴 Caffeine 缂撳瓨浣滀负 L1锛孯edis 浣滀负 L2
 */
public class SemanticCacheAdvisor implements Advisor {

    private static final Logger log = LoggerFactory.getLogger(SemanticCacheAdvisor.class);

    private final RedissonClient redissonClient;
    private final EmbeddingModel embeddingModel;

    /** 缂撳瓨鐩镐技搴﹂槇鍊?*/
    private double similarityThreshold = 0.85;

    /** 缂撳瓨杩囨湡鏃堕棿锛堝皬鏃讹級 */
    private long cacheTtlHours = 24;

    /** Redis 缂撳瓨 Key 鍓嶇紑 */
    private static final String CACHE_PREFIX = "spintale:semantic_cache:";

    /** 鏄惁鍚敤缂撳瓨 */
    private boolean enabled = true;

    /** 鏈湴 L1 缂撳瓨锛圕affeine锛?*/
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
        return AdvisorOrder.SEMANTIC_CACHE;
    }

    @Override
    public AdvisorRequest adviseRequest(AdvisorRequest request, AdvisorContext context) {
        if (!enabled || embeddingModel == null) {
            return request;
        }

        try {
            // 1. 鍏堟鏌?L1 鏈湴缂撳瓨
            String queryHash = hashQuery(request.getUserMessage());
            String cached = localCache.getIfPresent(queryHash);
            if (cached != null) {
                log.debug("L1 cache hit: query={}", truncate(request.getUserMessage(), 50));
                context.put(AdvisorContext.CACHE_HIT, true);
                context.put(AdvisorContext.CACHE_RESPONSE, cached);
                return request;
            }

            // 2. 妫?鏌?L2 Redis 缂撳瓨锛堝熀浜庡悜閲忕浉浼煎害锛?
            String redisCached = checkRedisCache(request.getUserMessage());
            if (redisCached != null) {
                log.info("L2 semantic cache hit: query={}", truncate(request.getUserMessage(), 50));
                // 鍥炲～ L1 缂撳瓨
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

        // 濡傛灉鏄紦瀛樺懡涓紝涓嶉渶瑕佸啀娆＄紦瀛?
        if (Boolean.TRUE.equals(context.get(AdvisorContext.CACHE_HIT, Boolean.class))) {
            return response;
        }

        try {
            String query = (String) context.get(AdvisorContext.ORIGINAL_QUERY);
            String content = response.getContent();

            if (query != null && content != null && !content.isEmpty()) {
                // 1. 鍐欏叆 L1 缂撳瓨
                String queryHash = hashQuery(query);
                localCache.put(queryHash, content);

                // 2. 鍐欏叆 L2 Redis 缂撳瓨
                writeToRedisCache(query, content);
            }

        } catch (Exception e) {
            log.warn("Semantic cache write failed: {}", e.getMessage());
        }

        return response;
    }

    /**
     * 妫?鏌?Redis 缂撳瓨锛堜娇鐢ㄥ悜閲忕浉浼煎害鍖归厤锛?
     * 鏀硅繘锛氫笉鍐嶄娇鐢?keys() 鍏ㄥ簱鎵弿
     */
    private String checkRedisCache(String query) {
        if (redissonClient == null) {
            return null;
        }

        try {
            // 鐢熸垚鏌ヨ鍚戦噺
            Embedding queryEmbedding = embeddingModel.embed(query).content();
            String queryHash = hashQuery(query);

            // 鑾峰彇缂撳瓨妗讹紙鎸夊悜閲忓搱甯屽垎妗讹紝闄愬埗鎵弿鑼冨洿锛?
            RMap<String, Object> bucket = redissonClient.getMap(CACHE_PREFIX + "bucket_" + Math.abs(queryHash.hashCode() % 16));
            if (bucket.isEmpty()) {
                return null;
            }

            // 閬嶅巻妗跺唴缂撳瓨椤癸紝璁＄畻鐩镐技搴?
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
     * 鍐欏叆 Redis 缂撳瓨
     */
    private void writeToRedisCache(String query, String response) {
        if (redissonClient == null) {
            return;
        }

        try {
            Embedding embedding = embeddingModel.embed(query).content();
            String queryHash = hashQuery(query);

            RMap<String, Object> bucket = redissonClient.getMap(CACHE_PREFIX + "bucket_" + Math.abs(queryHash.hashCode() % 16));
            bucket.put("vec_" + queryHash, toFloatArray(embedding));
            bucket.put("res_" + queryHash, response);
            bucket.put("qry_" + queryHash, query);
            bucket.put("ts_" + queryHash, System.currentTimeMillis());

            // 璁剧疆杩囨湡鏃堕棿
            bucket.expire(cacheTtlHours, TimeUnit.HOURS);

        } catch (Exception e) {
            log.error("Redis cache write failed: {}", e.getMessage());
        }
    }

    /**
     * 璁＄畻鍚戦噺鐩镐技搴?
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

    // ==================== 閰嶇疆鏂规硶 ====================

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
