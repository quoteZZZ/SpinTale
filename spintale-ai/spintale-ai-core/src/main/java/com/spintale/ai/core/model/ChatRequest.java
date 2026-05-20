package com.spintale.ai.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
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

    /** Single message content (convenience field) */
    private String message;

    /** System prompt/instruction */
    private String systemPrompt;

    /** Conversation messages */
    private List<ChatMessage> messages;

    /** Conversation history */
    private List<ChatMessage> history;

    /** Session ID for context tracking */
    private String sessionId;

    /** User ID for personalization */
    private String userId;

    /** Model name (optional, uses default if not specified) */
    private String model;

    /** Temperature for sampling (0.0 - 2.0) */
    private Double temperature;

    /** Maximum tokens to generate */
    private Integer maxTokens;

    /** Streaming mode flag */
    @Builder.Default
    private boolean streaming = false;

    /** Additional parameters for specific providers */
    private Map<String, Object> extraParams;

    public List<ChatMessage> getMessages() {
        if (messages == null) {
            messages = new ArrayList<>();
        }
        return messages;
    }

    public List<ChatMessage> getHistory() {
        if (history == null) {
            history = new ArrayList<>();
        }
        return history;
    }
}
