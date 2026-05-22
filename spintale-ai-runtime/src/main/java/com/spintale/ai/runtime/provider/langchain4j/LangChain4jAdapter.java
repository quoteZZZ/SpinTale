package com.spintale.ai.runtime.provider.langchain4j;

import com.spintale.ai.core.model.ChatMessage;
import com.spintale.ai.core.model.ChatRequest;
import com.spintale.ai.core.model.ChatResponse;
import com.spintale.ai.core.model.TokenUsage;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Boundary adapter between SpinTale AI contracts and LangChain4j types.
 */
public class LangChain4jAdapter {

    /**
     * Convert a LangChain4j response into the project-level response contract.
     */
    public static ChatResponse convertChatResponse(dev.langchain4j.model.chat.response.ChatResponse lcResponse) {
        return convertChatResponse(lcResponse, null);
    }

    /**
     * Convert a LangChain4j response and preserve the caller session.
     */
    public static ChatResponse convertChatResponse(dev.langchain4j.model.chat.response.ChatResponse lcResponse,
                                                   String sessionId) {
        if (lcResponse == null) {
            return null;
        }

        AiMessage aiMessage = lcResponse.aiMessage();
        TokenUsage tokenUsage = null;
        if (lcResponse.tokenUsage() != null) {
            tokenUsage = new TokenUsage(
                lcResponse.tokenUsage().inputTokenCount(),
                lcResponse.tokenUsage().outputTokenCount(),
                lcResponse.tokenUsage().totalTokenCount()
            );
        }

        List<ChatResponse.ToolCall> toolCalls = null;
        boolean hasToolExecutionRequests = aiMessage != null && aiMessage.hasToolExecutionRequests();
        if (hasToolExecutionRequests) {
            toolCalls = aiMessage.toolExecutionRequests().stream()
                .map(req -> new ChatResponse.ToolCall(
                    req.id(),
                    req.name(),
                    parseArguments(req.arguments()),
                    null
                ))
                .collect(Collectors.toList());
        }

        return ChatResponse.builder()
            .sessionId(sessionId)
            .content(aiMessage == null ? "" : aiMessage.text())
            .model(lcResponse.modelName())
            .tokenUsage(tokenUsage)
            .toolCalls(toolCalls)
            .finished(true)
            .requiresToolExecution(hasToolExecutionRequests)
            .finishReason(lcResponse.finishReason() != null ? lcResponse.finishReason().toString() : null)
            .build();
    }

    /**
     * Convert the full project request into LangChain4j messages.
     */
    public static List<dev.langchain4j.data.message.ChatMessage> convertMessages(ChatRequest request) {
        List<dev.langchain4j.data.message.ChatMessage> messages = new ArrayList<>();
        if (request == null) {
            return messages;
        }
        if (request.getMessages() != null && !request.getMessages().isEmpty()) {
            return convertMessages(request.getMessages());
        }
        if (isNotBlank(request.getSystemPrompt())) {
            messages.add(SystemMessage.from(request.getSystemPrompt()));
        }
        if (request.getHistory() != null && !request.getHistory().isEmpty()) {
            messages.addAll(convertMessages(request.getHistory()));
        }
        messages.add(UserMessage.from(defaultString(request.getMessage())));
        return messages;
    }

    /**
     * Convert project-level messages into LangChain4j messages.
     */
    public static List<dev.langchain4j.data.message.ChatMessage> convertMessages(List<ChatMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return List.of();
        }

        return messages.stream()
            .map(msg -> {
                String role = msg.getRole();
                if ("user".equalsIgnoreCase(role)) {
                    return UserMessage.from(msg.getContent());
                } else if ("assistant".equalsIgnoreCase(role) || "ai".equalsIgnoreCase(role)) {
                    return AiMessage.from(msg.getContent());
                } else if ("system".equalsIgnoreCase(role)) {
                    return SystemMessage.from(msg.getContent());
                }
                return UserMessage.from(msg.getContent());
            })
            .collect(Collectors.toList());
    }

    /**
     * Parse tool-call arguments into a map while preserving invalid payloads.
     */
    private static Map<String, Object> parseArguments(String argumentsJson) {
        if (argumentsJson == null || argumentsJson.trim().isEmpty()) {
            return Map.of();
        }

        try {
            return com.alibaba.fastjson2.JSON.parseObject(argumentsJson,
                new com.alibaba.fastjson2.TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            return Map.of("raw", argumentsJson);
        }
    }

    private static boolean isNotBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private static String defaultString(String value) {
        return value == null ? "" : value;
    }
}
