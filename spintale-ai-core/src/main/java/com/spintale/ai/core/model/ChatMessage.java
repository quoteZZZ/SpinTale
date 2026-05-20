package com.spintale.ai.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Chat message representing a single turn in conversation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    public static final String ROLE_SYSTEM = "system";
    public static final String ROLE_USER = "user";
    public static final String ROLE_ASSISTANT = "assistant";
    public static final String ROLE_TOOL = "tool";

    /** Message role: system, user, assistant, tool */
    private String role;

    /** Message content */
    private String content;

    /** Optional metadata */
    private Map<String, Object> metadata;

    public static ChatMessage system(String content) {
        return ChatMessage.builder()
                .role(ROLE_SYSTEM)
                .content(content)
                .build();
    }

    public static ChatMessage user(String content) {
        return ChatMessage.builder()
                .role(ROLE_USER)
                .content(content)
                .build();
    }

    public static ChatMessage assistant(String content) {
        return ChatMessage.builder()
                .role(ROLE_ASSISTANT)
                .content(content)
                .build();
    }
}
