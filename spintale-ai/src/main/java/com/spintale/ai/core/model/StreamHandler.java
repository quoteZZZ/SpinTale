package com.spintale.ai.core.model;

/**
 * 流式响应处理器
 */
public interface StreamHandler {

    /**
     * 处理 token 片段
     */
    void onToken(String token);

    /**
     * 处理完成事件
     */
    void onComplete(ChatResponse response);

    /**
     * 处理错误事件
     */
    void onError(Throwable error);
}
