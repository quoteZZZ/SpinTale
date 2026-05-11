package com.spintale.ai.tool;

/**
 * AI 工具接口
 */
public interface AiTool {
    
    /**
     * 工具名称
     */
    String getName();
    
    /**
     * 工具描述
     */
    String getDescription();
    
    /**
     * 执行工具
     * @param args 参数
     * @return 执行结果
     */
    String execute(String args);
}
