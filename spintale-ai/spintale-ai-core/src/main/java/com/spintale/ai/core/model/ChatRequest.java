package com.spintale.ai.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Chat request containing messages and configuration.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {

    /** Conversation messages */
    private List<ChatMessage> messages;

    /** Model name (optional, uses default if not specified) */
    private String model;

    /** Temperature for sampling (0.0 - 2.0) */
    private Double temperature;

    /** Maximum tokens to generate */
    private Integer maxTokens;

    /** Additional parameters for specific providers */
    private Map<String, Object> extraParams;

    /** Streaming mode flag */
    @Builder.Default
    private boolean streaming = false;
}
