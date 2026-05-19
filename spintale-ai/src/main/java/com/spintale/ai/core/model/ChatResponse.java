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
    @Builder.Default
    private Boolean finished = true;
    private String finishReason;
    private Map<String, Object> extraData;
    
    /**
     * 是否需要执行工具（ReAct 模式）
     */
    @Builder.Default
    private Boolean requiresToolExecution = false;
    
    /**
     * 推理轨迹（ReAct 模式的思考过程）
     */
    private String reasoningTrace;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ToolCall {
        private String id;
        private String name;
        private Map<String, Object> arguments;
        private String result;
    }
}
