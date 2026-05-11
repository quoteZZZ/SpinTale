package com.spintale.ai.core;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * AI 聊天响应
 */
@Data
@Builder
public class ChatResponse {
    
    /**
     * 会话 ID
     */
    private String sessionId;
    
    /**
     * AI 回复内容
     */
    private String content;
    
    /**
     * 使用的模型
     */
    private String model;
    
    /**
     * Token 使用统计
     */
    private TokenUsage tokenUsage;
    
    /**
     * 工具调用列表
     */
    private List<ToolCall> toolCalls;
    
    /**
     * 是否完成
     */
    @Builder.Default
    private Boolean finished = true;
    
    /**
     * 额外数据
     */
    private Map<String, Object> extraData;
}
