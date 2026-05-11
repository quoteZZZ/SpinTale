package com.spintale.ai.core;

/**
 * AI 聊天服务接口
 */
public interface AiChatService {
    
    /**
     * 简单聊天
     * @param message 用户消息
     * @return AI 回复
     */
    String chat(String message);
    
    /**
     * 带上下文的聊天
     * @param request 聊天请求
     * @return 聊天响应
     */
    ChatResponse chat(ChatRequest request);
    
    /**
     * 流式聊天
     * @param request 聊天请求
     * @return 流式响应处理器
     */
    void streamChat(ChatRequest request, StreamHandler handler);
}
