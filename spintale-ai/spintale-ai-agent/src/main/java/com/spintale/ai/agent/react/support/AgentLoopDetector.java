package com.spintale.ai.agent.react.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Agent循环检测器
 * 检测重复工具调用模式，防止死循环
 */
public class AgentLoopDetector {

    private static final Logger log = LoggerFactory.getLogger(AgentLoopDetector.class);

    private final int threshold;
    private final Map<String, Integer> toolCallCount = new HashMap<>();
    private final Map<String, Integer> toolArgsHashCount = new HashMap<>();

    public AgentLoopDetector() {
        this(3);
    }

    public AgentLoopDetector(int threshold) {
        this.threshold = threshold;
    }

    /**
     * 记录工具调用并检测循环
     * 
     * @param toolName 工具名称
     * @param argsJson 参数JSON
     * @return true表示检测到循环，应终止执行
     */
    public boolean checkAndRecord(String toolName, String argsJson) {
        toolCallCount.merge(toolName, 1, Integer::sum);
        
        String argsHash = toolName + ":" + (argsJson != null ? argsJson.hashCode() : 0);
        toolArgsHashCount.merge(argsHash, 1, Integer::sum);
        
        int count = toolCallCount.get(toolName);
        int exactMatchCount = toolArgsHashCount.get(argsHash);
        
        if (exactMatchCount > 1) {
            log.warn("Exact loop detected: tool '{}' with same args called {} times", 
                    toolName, exactMatchCount);
            return true;
        }
        
        if (count > threshold) {
            log.warn("Potential loop detected: tool '{}' called {} times (threshold: {})", 
                    toolName, count, threshold);
            return true;
        }
        
        return false;
    }

    /**
     * 获取工具调用次数
     */
    public int getCallCount(String toolName) {
        return toolCallCount.getOrDefault(toolName, 0);
    }

    /**
     * 重置检测状态
     */
    public void reset() {
        toolCallCount.clear();
        toolArgsHashCount.clear();
    }

    /**
     * 获取循环检测统计信息
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("toolCallCount", new HashMap<>(toolCallCount));
        stats.put("threshold", threshold);
        return stats;
    }
}
