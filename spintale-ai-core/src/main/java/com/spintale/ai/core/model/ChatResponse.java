package com.spintale.ai.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.List;

/**
 * Chat response from AI model.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {

    /** Response content */
    private String content;

    /** Model name used for generation */
    private String model;

    /** Session ID for conversation tracking */
    private String sessionId;

    /** Token usage statistics */
    private TokenUsage tokenUsage;

    /** Finish reason: stop, length, tool_calls, etc. */
    private String finishReason;

    /** Tool calls requested by the model. */
    private List<ToolCall> toolCalls;

    /** Whether tool execution is required before producing the final answer. */
    @Builder.Default
    private boolean requiresToolExecution = false;

    /** Whether the response is finished (for streaming) */
    @Builder.Default
    private Boolean finished = true;

    /** Additional metadata */
    private Map<String, Object> metadata;

    /** Extra data for advisor processing */
    private Map<String, Object> extraData;

    /** Whether the response is complete (for streaming) */
    @Builder.Default
    private boolean complete = true;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ToolCall {
        private String id;
        private String name;
        private Map<String, Object> arguments;
        private String type;
    }
}
