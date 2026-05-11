package com.spintale.ai.agent;

import java.util.Map;

/**
 * AI Agent 执行结果
 */
public interface AgentResult {
    
    /**
     * 是否成功
     */
    boolean isSuccess();
    
    /**
     * 最终回复内容
     */
    String getContent();
    
    /**
     * 执行过程中的中间步骤
     */
    Map<String, Object> getSteps();
    
    /**
     * 使用的工具列表
     */
    java.util.List<String> getUsedTools();
    
    /**
     * Token 使用情况
     */
    Object getTokenUsage();
}
