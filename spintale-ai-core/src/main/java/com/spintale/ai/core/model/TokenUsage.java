package com.spintale.ai.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Token usage statistics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenUsage {

    /** Input tokens count */
    private int inputTokens;

    /** Output tokens count */
    private int outputTokens;

    /** Total tokens count */
    private int totalTokens;

    /**
     * Compatibility method for prompt tokens (alias for inputTokens).
     */
    public int getPromptTokens() {
        return inputTokens;
    }

    /**
     * Compatibility method for completion tokens (alias for outputTokens).
     */
    public int getCompletionTokens() {
        return outputTokens;
    }

    public static TokenUsage empty() {
        return new TokenUsage(0, 0, 0);
    }

    public TokenUsage add(TokenUsage other) {
        if (other == null) {
            return this;
        }
        return new TokenUsage(
                this.inputTokens + other.inputTokens,
                this.outputTokens + other.outputTokens,
                this.totalTokens + other.totalTokens
        );
    }
}
