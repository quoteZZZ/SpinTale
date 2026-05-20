package com.spintale.ai.agent.react.api;

/**
 * Agent 执行回调接口
 */
public interface AgentCallback {
    
    /**
     * 思考步骤回调
     * @param thought 思考内容
     */
    void onThought(String thought);
    
    /**
     * 工具调用回调
     * @param toolName 工具名称
     * @param input 输入参数
     */
    void onToolCall(String toolName, String input);
    
    /**
     * 工具执行结果回调
     * @param toolName 工具名称
     * @param output 输出结果
     */
    void onToolResult(String toolName, String output);
    
    /**
     * 最终回复回调
     * @param content 最终内容
     */
    void onFinalResponse(String content);
    
    /**
     * 错误回调
     * @param error 错误信息
     */
    void onError(String error);
}
