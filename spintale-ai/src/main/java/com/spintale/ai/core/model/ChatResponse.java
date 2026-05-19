package com.spintale.ai.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 聊天响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {

    private String sessionId;
    private String content;
    private String model;
    private TokenUsage tokenUsage;
    private List<ToolCall> toolCalls;
    private Boolean finished = true;
    private String finishReason;
    private Map<String, Object> extraData;
    
    /**
     * 是否需要执行工具（ReAct 模式）
     */
    private Boolean requiresToolExecution = false;
    
    /**
     * 推理轨迹（ReAct 模式的思考过程）
     */
    private String reasoningTrace;
}
