package com.spintale.ai.router;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 智能模型路由器
 * 根据查询复杂度动态选择小模型或大模型处理
 */
@Slf4j
@Service
public class ModelRouterService {

    private static final double COMPLEXITY_THRESHOLD = 0.6; // 复杂度阈值
    
    private final LocalSmallModelService smallModelService;

    public ModelRouterService(LocalSmallModelService smallModelService) {
        this.smallModelService = smallModelService;
    }

    /**
     * 路由决策：判断使用小模型还是大模型
     * @param query 用户查询
     * @return 路由结果
     */
    public RoutingResult route(String query) {
        log.info("执行模型路由：query={}", query);

        double complexity = calculateComplexity(query);
        
        // 检查小模型是否可用
        boolean smallModelAvailable = smallModelService.isAvailable();
        boolean useSmallModel = smallModelAvailable && complexity < COMPLEXITY_THRESHOLD;
        
        String selectedModel = useSmallModel ? smallModelService.getModelName() : "large-model";
        long estimatedLatency = useSmallModel ? 50 : 800; // ms
        double estimatedCost = useSmallModel ? 0.001 : 0.02; // USD

        log.info("路由决策：复杂度={:.2f}, 小模型可用={}, 选择模型={}, 预估延迟={}ms, 预估成本=${}", 
                 complexity, smallModelAvailable, selectedModel, estimatedLatency, estimatedCost);

        return new RoutingResult(selectedModel, complexity, useSmallModel, estimatedLatency, estimatedCost);
    }

    /**
     * 计算查询复杂度
     * 基于关键词、句子长度、逻辑词等特征
     */
    private double calculateComplexity(String query) {
        if (query == null || query.isEmpty()) {
            return 0.0;
        }

        double score = 0.0;

        // 1. 长度特征（归一化到 0-1）
        int length = query.length();
        double lengthScore = Math.min(length / 200.0, 1.0);
        score += lengthScore * 0.3;

        // 2. 逻辑关键词
        String[] logicKeywords = {"如果", "那么", "因为", "所以", "比较", "分析", "为什么", "如何", "步骤", "计划"};
        long logicCount = java.util.Arrays.stream(logicKeywords)
                .filter(query::contains)
                .count();
        double logicScore = Math.min(logicCount / 5.0, 1.0);
        score += logicScore * 0.4;

        // 3. 多任务特征（包含多个动词或问号）
        long questionMarks = query.chars().filter(ch -> ch == '?').count();
        long verbs = java.util.regex.Pattern.compile("(请|需要|想要|帮我|给我)").matcher(query).results().count();
        double multitaskScore = Math.min((questionMarks + verbs) / 4.0, 1.0);
        score += multitaskScore * 0.3;

        return Math.min(score, 1.0);
    }

    /**
     * 路由结果记录
     */
    public record RoutingResult(
            String selectedModel,
            double complexity,
            boolean useSmallModel,
            long estimatedLatencyMs,
            double estimatedCostUsd
    ) {}

    /**
     * 获取实际的小模型实例供调用
     */
    public dev.langchain4j.model.chat.ChatLanguageModel getActualModel(RoutingResult result) {
        if (result.useSmallModel()) {
            return smallModelService.getChatModel();
        }
        return null; // 大模型由其他地方处理
    }
}
