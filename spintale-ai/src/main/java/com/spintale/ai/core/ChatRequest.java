package com.spintale.ai.core;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * AI 聊天请求
 */
@Data
@Builder
public class ChatRequest {
    
    /**
     * 会话 ID
     */
    private String sessionId;
    
    /**
     * 用户消息
     */
    private String message;
    
    /**
     * 系统提示词
     */
    private String systemPrompt;
    
    /**
     * 对话历史
     */
    private List<ChatMessage> history;
    
    /**
     * 温度参数 (0-1)
     */
    @Builder.Default
    private Double temperature = 0.7;
    
    /**
     * 最大 token 数
     */
    @Builder.Default
    private Integer maxTokens = 2048;
    
    /**
     * 是否启用流式输出
     */
    @Builder.Default
    private Boolean stream = false;
    
    /**
     * 工具/函数列表
     */
    private List<ToolDefinition> tools;
    
    /**
     * 额外参数
     */
    private Map<String, Object> extraParams;
}
