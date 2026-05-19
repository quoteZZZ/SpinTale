package com.spintale.ai.observability;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * AI 指标收集器
 * 
 * 用于收集和监控 AI 服务的各种指标：
 * - Token 使用情况
 * - 响应时间
 * - 错误率
 * - 调用次数
 */
@Slf4j
public class AiMetricsCollector {
    
    /**
     * 模型调用统计
     */
    private final Map<String, ModelMetrics> modelMetrics = new ConcurrentHashMap<>();
    
    /**
     * 记录一次成功的 API 调用
     */
    public CallContext startCall(String modelName, String operation) {
        return new CallContext(modelName, operation);
    }
    
    /**
     * 获取指定模型的指标
     */
    public ModelMetrics getModelMetrics(String modelName) {
        return modelMetrics.computeIfAbsent(modelName, k -> new ModelMetrics());
    }
    
    /**
     * 重置所有指标
     */
    public void resetAllMetrics() {
        modelMetrics.clear();
        log.info("All metrics reset");
    }
    
    /**
     * 调用上下文，用于跟踪单次调用的完整生命周期
     */
    @Data
    public class CallContext {
        private final String modelName;
        private final String operation;
        private final LocalDateTime startTime;
        private int promptTokens;
        private int completionTokens;
        private boolean success;
        private Exception error;
        
        public CallContext(String modelName, String operation) {
            this.modelName = modelName;
            this.operation = operation;
            this.startTime = LocalDateTime.now();
            this.success = false;
        }
        
        /**
         * 记录成功调用
         */
        public void recordSuccess(int promptTokens, int completionTokens) {
            this.promptTokens = promptTokens;
            this.completionTokens = completionTokens;
            this.success = true;
            
            // 更新模型指标
            ModelMetrics metrics = getModelMetrics(modelName);
            metrics.recordSuccess(promptTokens, completionTokens);
            
            log.debug("Recorded successful call: model={}, operation={}, promptTokens={}, completionTokens={}",
                    modelName, operation, promptTokens, completionTokens);
        }
        
        /**
         * 记录失败调用
         */
        public void recordFailure(Exception error) {
            this.error = error;
            this.success = false;
            
            // 更新模型指标
            ModelMetrics metrics = getModelMetrics(modelName);
            metrics.recordFailure();
            
            log.warn("Recorded failed call: model={}, operation={}, error={}",
                    modelName, operation, error.getMessage());
        }
    }
    
    /**
     * 模型指标统计
     */
    @Data
    public static class ModelMetrics {
        private final AtomicLong totalCalls = new AtomicLong(0);
        private final AtomicLong successfulCalls = new AtomicLong(0);
        private final AtomicLong failedCalls = new AtomicLong(0);
        private final AtomicLong totalPromptTokens = new AtomicLong(0);
        private final AtomicLong totalCompletionTokens = new AtomicLong(0);
        private final AtomicLong totalResponseTimeMs = new AtomicLong(0);
        
        /**
         * 记录成功调用
         */
        public void recordSuccess(int promptTokens, int completionTokens) {
            totalCalls.incrementAndGet();
            successfulCalls.incrementAndGet();
            totalPromptTokens.addAndGet(promptTokens);
            totalCompletionTokens.addAndGet(completionTokens);
        }
        
        /**
         * 记录失败调用
         */
        public void recordFailure() {
            totalCalls.incrementAndGet();
            failedCalls.incrementAndGet();
        }
        
        /**
         * 获取成功率
         */
        public double getSuccessRate() {
            long total = totalCalls.get();
            if (total == 0) return 1.0;
            return (double) successfulCalls.get() / total;
        }
        
        /**
         * 获取平均响应时间（毫秒）
         */
        public double getAverageResponseTimeMs() {
            long total = totalCalls.get();
            if (total == 0) return 0.0;
            return (double) totalResponseTimeMs.get() / total;
        }
        
        /**
         * 获取总 Token 数
         */
        public long getTotalTokens() {
            return totalPromptTokens.get() + totalCompletionTokens.get();
        }
    }
}