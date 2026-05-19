package com.spintale.ai.capability.advisor;

import java.util.HashMap;
import java.util.Map;

/**
 * Advisor 上下文
 * 在整个 Advisor 链中共享数据的容器
 *
 * 典型用途：
 * - MemoryAdvisor 将检索到的记忆放入上下文
 * - RAGAdvisor 将检索到的文档放入上下文
 * - HallucinationAdvisor 从上下文读取参考文档进行检测
 */
public class AdvisorContext {

    private final Map<String, Object> data;

    public AdvisorContext() {
        this.data = new HashMap<>();
    }

    /**
     * 存储数据
     */
    public void put(String key, Object value) {
        data.put(key, value);
    }

    /**
     * 获取数据
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> type) {
        Object value = data.get(key);
        if (value == null) {
            return null;
        }
        return (T) value;
    }

    /**
     * 获取数据（带默认值）
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> type, T defaultValue) {
        Object value = data.get(key);
        if (value == null) {
            return defaultValue;
        }
        return (T) value;
    }

    /**
     * 获取原始数据
     */
    public Object get(String key) {
        return data.get(key);
    }

    /**
     * 是否包含指定键
     */
    public boolean contains(String key) {
        return data.containsKey(key);
    }

    /**
     * 移除数据
     */
    public void remove(String key) {
        data.remove(key);
    }

    /**
     * 清空上下文
     */
    public void clear() {
        data.clear();
    }

    // ==================== 预定义的上下文键 ====================

    /** 检索到的 RAG 文档列表 */
    public static final String RETRIEVED_DOCUMENTS = "retrieved_documents";

    /** 检索到的长期记忆列表 */
    public static final String RETRIEVED_MEMORIES = "retrieved_memories";

    /** 缓存命中标记 */
    public static final String CACHE_HIT = "cache_hit";

    /** 缓存响应内容 */
    public static final String CACHE_RESPONSE = "cache_response";

    /** 安全检查结果 */
    public static final String SAFETY_CHECK_PASSED = "safety_check_passed";

    /** 幻觉检测结果 */
    public static final String HALLUCINATION_RESULT = "hallucination_result";

    /** 模型路由结果 */
    public static final String ROUTING_RESULT = "routing_result";

    /** 原始用户查询（查询改写前） */
    public static final String ORIGINAL_QUERY = "original_query";

    /** Token 统计 */
    public static final String TOKEN_STATS = "token_stats";

    /** 执行耗时(ms) */
    public static final String EXECUTION_DURATION_MS = "execution_duration_ms";
}
