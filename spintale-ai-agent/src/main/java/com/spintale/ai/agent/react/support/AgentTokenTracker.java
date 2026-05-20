package com.spintale.ai.agent.react.support;

import dev.langchain4j.model.output.TokenUsage;

import java.util.HashMap;
import java.util.Map;

/**
 * Agent Token统计跟踪器
 * 跟踪整个执行过程中的Token使用情况
 */
public class AgentTokenTracker {

    private long inputTokens = 0;
    private long outputTokens = 0;
    private long totalTokens = 0;
    private final Map<Integer, TokenUsage> iterationUsage = new HashMap<>();

    /**
     * 累加Token使用量
     */
    public void accumulate(TokenUsage usage) {
        if (usage == null) {
            return;
        }
        
        inputTokens += usage.inputTokenCount();
        outputTokens += usage.outputTokenCount();
        totalTokens += usage.totalTokenCount();
    }

    /**
     * 记录单次迭代的Token使用
     */
    public void recordIteration(int iteration, TokenUsage usage) {
        if (usage != null) {
            iterationUsage.put(iteration, usage);
            accumulate(usage);
        }
    }

    /**
     * 获取累计Token使用量
     */
    public TokenUsage getTotalUsage() {
        return new TokenUsage((int) inputTokens, (int) outputTokens, (int) totalTokens);
    }

    /**
     * 获取详细统计信息
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("inputTokens", inputTokens);
        stats.put("outputTokens", outputTokens);
        stats.put("totalTokens", totalTokens);
        stats.put("iterations", iterationUsage.size());
        return stats;
    }

    /**
     * 重置统计
     */
    public void reset() {
        inputTokens = 0;
        outputTokens = 0;
        totalTokens = 0;
        iterationUsage.clear();
    }

    /**
     * 转换为项目的TokenUsage类型
     */
    public com.spintale.ai.core.model.TokenUsage toSpintaleTokenUsage() {
        return new com.spintale.ai.core.model.TokenUsage(
                (int) inputTokens,
                (int) outputTokens,
                (int) totalTokens
        );
    }
}
