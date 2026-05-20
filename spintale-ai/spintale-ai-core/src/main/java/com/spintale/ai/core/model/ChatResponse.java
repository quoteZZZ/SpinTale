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

    /** Token usage statistics */
    private TokenUsage tokenUsage;

    /** Finish reason: stop, length, tool_calls, etc. */
    private String finishReason;

    /** Additional metadata */
    private Map<String, Object> metadata;

    /** Whether the response is complete (for streaming) */
    @Builder.Default
    private boolean complete = true;
}
