package com.spintale.ai.core;

/**
 * 流式响应处理器
 */
public interface StreamHandler {
    
    /**
     * 处理 token 片段
     * @param token token 片段
     */
    void onToken(String token);
    
    /**
     * 处理完成事件
     * @param response 完整响应
     */
    void onComplete(ChatResponse response);
    
    /**
     * 处理错误事件
     * @param error 错误信息
     */
    void onError(Throwable error);
}
