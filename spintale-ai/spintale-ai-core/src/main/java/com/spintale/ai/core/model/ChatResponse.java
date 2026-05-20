package com.spintale.ai.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

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
}
