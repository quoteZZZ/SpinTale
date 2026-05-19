package com.spintale.ai.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 聊天消息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    private String role;
    private String content;
    private String toolCallId;
    private String toolName;

    public static ChatMessage system(String content) { return of("system", content); }
    public static ChatMessage user(String content) { return of("user", content); }
    public static ChatMessage assistant(String content) { return of("assistant", content); }

    public static ChatMessage of(String role, String content) {
        return ChatMessage.builder()
                .role(role)
                .content(content)
                .build();
    }
}
