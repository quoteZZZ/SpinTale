package com.spintale.ai.observability;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * AI 成本监控器
 * 
 * 跟踪和监控Token使用成本：
 * - 按模型统计Token使用量
 * - 计算实际成本
 * - 预算告警
 * - 每日/每月报告
 */
@Slf4j
@Data
public class CostMonitor {
    
    /**
     * 模型定价配置（每1K tokens的价格，USD）
     */
    private final Map<String, ModelPricing> modelPricing;
    
    /**
     * 每日使用情况
     */
    private final Map<LocalDate, DailyUsage> dailyUsage;
    
    /**
     * 预算限制（USD）
     */
    private BigDecimal budgetLimit;
    
    /**
     * 当前周期已使用金额
     */
    private BigDecimal currentSpending = BigDecimal.ZERO;
    
    /**
     * 告警阈值（百分比）
     */
    private double alertThreshold = 0.8;
    
    public CostMonitor() {
        this.modelPricing = new ConcurrentHashMap<>();
        this.dailyUsage = new ConcurrentHashMap<>();
        this.budgetLimit = new BigDecimal("100"); // 默认$100
        
        // 初始化常见模型的定价
        initDefaultPricing();
    }
    
    /**
     * 记录Token使用
     */
    public void recordUsage(String model, int promptTokens, int completionTokens) {
        LocalDate today = LocalDate.now();
        
        // 获取或创建今日使用记录
        DailyUsage usage = dailyUsage.computeIfAbsent(today, k -> new DailyUsage());
        
        // 更新使用量
        usage.recordUsage(model, promptTokens, completionTokens);
        
        // 计算成本
        ModelPricing pricing = modelPricing.get(model);
        if (pricing != null) {
            BigDecimal cost = pricing.calculateCost(promptTokens, completionTokens);
            usage.addCost(model, cost);
            currentSpending = currentSpending.add(cost);
            
            log.debug("Recorded usage: model={}, prompt={}, completion={}, cost=${}",
                    model, promptTokens, completionTokens, cost);
            
            // 检查预算
            checkBudget();
        }
    }
    
    /**
     * 获取今日使用情况
     */
    public DailyUsage getTodayUsage() {
        return dailyUsage.getOrDefault(LocalDate.now(), new DailyUsage());
    }
    
    /**
     * 获取本月总花费
     */
    public BigDecimal getMonthlySpending() {
        LocalDate firstDayOfMonth = LocalDate.now().withDayOfMonth(1);
        BigDecimal total = BigDecimal.ZERO;
        
        for (Map.Entry<LocalDate, DailyUsage> entry : dailyUsage.entrySet()) {
            if (!entry.getKey().isBefore(firstDayOfMonth)) {
                total = total.add(entry.getValue().getTotalCost());
            }
        }
        
        return total;
    }
    
    /**
     * 设置预算
     */
    public void setBudget(BigDecimal budget) {
        this.budgetLimit = budget;
        log.info("Budget set to: ${}", budget);
    }
    
    /**
     * 重置今日统计
     */
    public void resetDailyStats() {
        dailyUsage.clear();
        currentSpending = BigDecimal.ZERO;
        log.info("Daily stats reset");
    }
    
    /**
     * 检查预算是否超标
     */
    private void checkBudget() {
        if (budgetLimit != null && budgetLimit.compareTo(BigDecimal.ZERO) > 0) {
            double usagePercent = currentSpending.doubleValue() / budgetLimit.doubleValue();
            
            if (usagePercent >= alertThreshold) {
                log.warn("Budget alert: ${}/${} ({}%)",
                        currentSpending, budgetLimit, String.format("%.1f", usagePercent * 100));
                
                // 可以触发告警通知
                triggerBudgetAlert(usagePercent);
            }
        }
    }
    
    /**
     * 触发预算告警
     */
    private void triggerBudgetAlert(double usagePercent) {
        log.warn("Budget alert: {}% of budget used", String.format("%.1f", usagePercent * 100));
    }
    
    /**
     * 初始化默认定价
     */
    private void initDefaultPricing() {
        // OpenAI GPT-4
        modelPricing.put("gpt-4", new ModelPricing(
                new BigDecimal("0.03"),  // prompt: $0.03/1K tokens
                new BigDecimal("0.06")   // completion: $0.06/1K tokens
        ));
        
        // OpenAI GPT-3.5-Turbo
        modelPricing.put("gpt-3.5-turbo", new ModelPricing(
                new BigDecimal("0.0015"),
                new BigDecimal("0.002")
        ));
        
        // Azure OpenAI (相同定价)
        modelPricing.put("azure-gpt-4", new ModelPricing(
                new BigDecimal("0.03"),
                new BigDecimal("0.06")
        ));
        
        log.info("Initialized default pricing for {} models", modelPricing.size());
    }
    
    /**
     * 模型定价配置
     */
    @Data
    public static class ModelPricing {
        private BigDecimal promptPricePer1K;
        private BigDecimal completionPricePer1K;
        
        public ModelPricing(BigDecimal promptPricePer1K, BigDecimal completionPricePer1K) {
            this.promptPricePer1K = promptPricePer1K;
            this.completionPricePer1K = completionPricePer1K;
        }
        
        /**
         * 计算成本
         */
        public BigDecimal calculateCost(int promptTokens, int completionTokens) {
            BigDecimal promptCost = BigDecimal.valueOf(promptTokens)
                    .divide(BigDecimal.valueOf(1000))
                    .multiply(promptPricePer1K);
            
            BigDecimal completionCost = BigDecimal.valueOf(completionTokens)
                    .divide(BigDecimal.valueOf(1000))
                    .multiply(completionPricePer1K);
            
            return promptCost.add(completionCost);
        }
    }
    
    /**
     * 每日使用情况
     */
    @Data
    public static class DailyUsage {
        private final Map<String, AtomicLong> modelPromptTokens = new ConcurrentHashMap<>();
        private final Map<String, AtomicLong> modelCompletionTokens = new ConcurrentHashMap<>();
        private final Map<String, BigDecimal> modelCosts = new ConcurrentHashMap<>();
        private BigDecimal totalCost = BigDecimal.ZERO;
        
        public void recordUsage(String model, int promptTokens, int completionTokens) {
            modelPromptTokens.computeIfAbsent(model, k -> new AtomicLong())
                    .addAndGet(promptTokens);
            modelCompletionTokens.computeIfAbsent(model, k -> new AtomicLong())
                    .addAndGet(completionTokens);
        }
        
        public void addCost(String model, BigDecimal cost) {
            modelCosts.merge(model, cost, BigDecimal::add);
            totalCost = totalCost.add(cost);
        }
        
        public long getTotalPromptTokens() {
            return modelPromptTokens.values().stream()
                    .mapToLong(AtomicLong::get)
                    .sum();
        }
        
        public long getTotalCompletionTokens() {
            return modelCompletionTokens.values().stream()
                    .mapToLong(AtomicLong::get)
                    .sum();
        }
    }
}
