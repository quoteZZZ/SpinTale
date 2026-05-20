package com.spintale.ai.core.options;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Chat model configuration options.
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ChatOptions {

    /** Model name */
    private String model;

    /** Temperature for sampling (0.0 - 2.0) */
    @Builder.Default
    private Double temperature = 0.7;

    /** Maximum tokens to generate */
    @Builder.Default
    private Integer maxTokens = 2048;

    /** Top-p sampling parameter */
    private Double topP;

    /** Top-k sampling parameter */
    private Integer topK;

    /** Stop sequences */
    private java.util.List<String> stopSequences;

    /** Presence penalty (-2.0 to 2.0) */
    private Double presencePenalty;

    /** Frequency penalty (-2.0 to 2.0) */
    private Double frequencyPenalty;

    /** Additional provider-specific parameters */
    private java.util.Map<String, Object> customParams;

    /**
     * Merge with another options (non-null values override).
     *
     * @param other options to merge
     * @return merged options
     */
    public ChatOptions merge(ChatOptions other) {
        if (other == null) {
            return this;
        }

        ChatOptions.ChatOptionsBuilder builder = this.toBuilder();
        
        if (other.getModel() != null) {
            builder.model(other.getModel());
        }
        if (other.getTemperature() != null) {
            builder.temperature(other.getTemperature());
        }
        if (other.getMaxTokens() != null) {
            builder.maxTokens(other.getMaxTokens());
        }
        if (other.getTopP() != null) {
            builder.topP(other.getTopP());
        }
        if (other.getTopK() != null) {
            builder.topK(other.getTopK());
        }
        if (other.getStopSequences() != null) {
            builder.stopSequences(other.getStopSequences());
        }
        if (other.getPresencePenalty() != null) {
            builder.presencePenalty(other.getPresencePenalty());
        }
        if (other.getFrequencyPenalty() != null) {
            builder.frequencyPenalty(other.getFrequencyPenalty());
        }
        if (other.getCustomParams() != null) {
            if (this.customParams == null) {
                builder.customParams(other.getCustomParams());
            } else {
                java.util.Map<String, Object> merged = new java.util.HashMap<>(this.customParams);
                merged.putAll(other.getCustomParams());
                builder.customParams(merged);
            }
        }
        
        return builder.build();
    }
}
