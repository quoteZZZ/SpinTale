package com.spintale.ai.core.metrics;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A/B 测试实验定义
 * 
 * 用于对比不同模型或配置的效果
 */
@Data
@Slf4j
public class Experiment {
    
    /**
     * 实验ID
     */
    private String experimentId;
    
    /**
     * 实验名称
     */
    private String name;
    
    /**
     * 实验描述
     */
    private String description;
    
    /**
     * 变体配置（模型A、模型B等）
     */
    private Map<String, Variant> variants;
    
    /**
     * 流量分配（各变体的百分比，总和应为100）
     */
    private Map<String, Integer> trafficAllocation;
    
    /**
     * 评估指标
     */
    private Map<String, Metric> metrics;
    
    /**
     * 是否激活
     */
    private boolean active = false;
    
    /**
     * 开始时间
     */
    private Long startTime;
    
    /**
     * 结束时间
     */
    private Long endTime;
    
    /**
     * 总请求数
     */
    private final AtomicLong totalRequests = new AtomicLong(0);
    
    public Experiment() {
        this.variants = new ConcurrentHashMap<>();
        this.trafficAllocation = new ConcurrentHashMap<>();
        this.metrics = new ConcurrentHashMap<>();
    }
    
    /**
     * 添加变体
     */
    public void addVariant(String variantId, Variant variant, int trafficPercent) {
        variants.put(variantId, variant);
        trafficAllocation.put(variantId, trafficPercent);
        log.info("Added variant: {} with {}% traffic", variantId, trafficPercent);
    }
    
    /**
     * 根据用户ID选择变体（一致性哈希）
     */
    public String selectVariant(String userId) {
        if (!active || variants.isEmpty()) {
            return null;
        }
        
        // 使用用户ID的哈希值决定变体
        int hash = Math.abs(userId.hashCode() % 100);
        int cumulative = 0;
        
        for (Map.Entry<String, Integer> entry : trafficAllocation.entrySet()) {
            cumulative += entry.getValue();
            if (hash < cumulative) {
                return entry.getKey();
            }
        }
        
        // 默认返回第一个变体
        return variants.keySet().iterator().next();
    }
    
    /**
     * 记录请求
     */
    public void recordRequest(String variantId, Map<String, Object> metrics) {
        totalRequests.incrementAndGet();
        
        Variant variant = variants.get(variantId);
        if (variant != null) {
            variant.recordRequest(metrics);
        }
    }
    
    /**
     * 获取实验结果
     */
    public ExperimentResult getResult() {
        ExperimentResult result = new ExperimentResult();
        result.setExperimentId(experimentId);
        result.setName(name);
        result.setTotalRequests(totalRequests.get());
        
        for (Map.Entry<String, Variant> entry : variants.entrySet()) {
            result.addVariantResult(entry.getKey(), entry.getValue().getResult());
        }
        
        return result;
    }
    
    /**
     * 变体配置
     */
    @Data
    public static class Variant {
        private String variantId;
        private String modelName;
        private Map<String, Object> config;
        private final AtomicLong requestCount = new AtomicLong(0);
        private final AtomicLong successCount = new AtomicLong(0);
        private final AtomicLong totalLatency = new AtomicLong(0);
        private final AtomicLong totalTokens = new AtomicLong(0);
        
        public Variant() {
            this.config = new ConcurrentHashMap<>();
        }
        
        public void recordRequest(Map<String, Object> metrics) {
            requestCount.incrementAndGet();
            
            Boolean success = (Boolean) metrics.get("success");
            if (Boolean.TRUE.equals(success)) {
                successCount.incrementAndGet();
            }
            
            Long latency = (Long) metrics.get("latency");
            if (latency != null) {
                totalLatency.addAndGet(latency);
            }
            
            Integer tokens = (Integer) metrics.get("tokens");
            if (tokens != null) {
                totalTokens.addAndGet(tokens);
            }
        }
        
        public VariantResult getResult() {
            VariantResult result = new VariantResult();
            long requests = requestCount.get();
            
            result.setRequestCount(requests);
            result.setSuccessCount(successCount.get());
            result.setSuccessRate(requests > 0 ? (double) successCount.get() / requests : 0.0);
            result.setAvgLatency(requests > 0 ? totalLatency.get() / requests : 0);
            result.setAvgTokens(requests > 0 ? totalTokens.get() / requests : 0);
            
            return result;
        }
    }
    
    /**
     * 评估指标
     */
    @Data
    public static class Metric {
        private String metricId;
        private String name;
        private String description;
        private MetricType type;
        
        public enum MetricType {
            LATENCY,      // 延迟
            ACCURACY,     // 准确性
            COST,         // 成本
            SATISFACTION  // 满意度
        }
    }
    
    /**
     * 实验结果
     */
    @Data
    public static class ExperimentResult {
        private String experimentId;
        private String name;
        private long totalRequests;
        private Map<String, VariantResult> variantResults;
        
        public ExperimentResult() {
            this.variantResults = new ConcurrentHashMap<>();
        }
        
        public void addVariantResult(String variantId, VariantResult result) {
            variantResults.put(variantId, result);
        }
    }
    
    /**
     * 变体结果
     */
    @Data
    public static class VariantResult {
        private long requestCount;
        private long successCount;
        private double successRate;
        private long avgLatency;
        private long avgTokens;
    }
}
