package com.spintale.ai.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 聊天请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {

    private String message;
    private String systemPrompt;
    private List<ChatMessage> messages;
    private List<ChatMessage> history;
    private String sessionId;
    private String userId;
    private Double temperature;
    private Integer maxTokens;
    private Boolean stream;
    private Map<String, Object> extraParams;
}