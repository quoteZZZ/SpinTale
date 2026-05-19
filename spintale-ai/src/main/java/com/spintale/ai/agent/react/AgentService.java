package com.spintale.ai.agent.react;

import java.util.List;

/**
 * AI Agent 服务接口
 */
public interface AgentService {
    
    /**
     * 执行简单任务
     * @param task 任务描述
     * @return 执行结果
     */
    AgentResult execute(String task);
    
    /**
     * 执行任务（带最大迭代次数）
     * @param task 任务描述
     * @param maxIterations 最大迭代次数
     * @return 执行结果
     */
    AgentResult execute(String task, int maxIterations);
    
    /**
     * 执行任务（自定义工具集）
     * @param task 任务描述
     * @param toolNames 使用的工具名称列表
     * @return 执行结果
     */
    AgentResult executeWithTools(String task, List<String> toolNames);
    
    /**
     * 流式执行任务
     * @param task 任务描述
     * @param callback 回调函数，接收中间步骤更新
     */
    void executeStreaming(String task, AgentCallback callback);
}
