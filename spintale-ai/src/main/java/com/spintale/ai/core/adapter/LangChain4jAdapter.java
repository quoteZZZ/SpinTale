package com.spintale.ai.core.adapter;

import com.spintale.ai.core.model.ChatMessage;
import com.spintale.ai.core.model.ChatResponse;
import com.spintale.ai.core.model.TokenUsage;
import com.spintale.ai.core.model.ToolCall;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * LangChain4j 适配器
 * 
 * 将 LangChain4j 的模型和响应转换为项目自定义的模型
 */
public class LangChain4jAdapter {

    /**
     * 将 LangChain4j ChatResponse 转换为项目自定义的 ChatResponse
     */
    public static ChatResponse convertChatResponse(dev.langchain4j.model.chat.response.ChatResponse lcResponse) {
        if (lcResponse == null) {
            return null;
        }

        AiMessage aiMessage = lcResponse.aiMessage();
        
        // 转换 TokenUsage
        TokenUsage tokenUsage = null;
        if (lcResponse.tokenUsage() != null) {
            tokenUsage = new TokenUsage(
                lcResponse.tokenUsage().inputTokenCount(),
                lcResponse.tokenUsage().outputTokenCount(),
                lcResponse.tokenUsage().totalTokenCount()
            );
        }

        // 转换 ToolCalls
        List<ToolCall> toolCalls = null;
        if (aiMessage.hasToolExecutionRequests()) {
            toolCalls = aiMessage.toolExecutionRequests().stream()
                .map(req -> new ToolCall(
                    req.id(),
                    req.name(),
                    parseArguments(req.arguments()),
                    null
                ))
                .collect(Collectors.toList());
        }

        return ChatResponse.builder()
            .content(aiMessage.text())
            .tokenUsage(tokenUsage)
            .toolCalls(toolCalls)
            .requiresToolExecution(aiMessage.hasToolExecutionRequests())
            .finishReason(lcResponse.finishReason() != null ? lcResponse.finishReason().toString() : null)
            .build();
    }

    /**
     * 将项目自定义的 ChatMessage 列表转换为 LangChain4j 的 ChatMessage 列表
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
                    return new dev.langchain4j.data.message.SystemMessage(msg.getContent());
                }
                return UserMessage.from(msg.getContent());
            })
            .collect(Collectors.toList());
    }

    /**
     * 解析工具参数 JSON 字符串为 Map
     */
    private static Map<String, Object> parseArguments(String argumentsJson) {
        if (argumentsJson == null || argumentsJson.trim().isEmpty()) {
            return Map.of();
        }
        
        try {
            // 使用 FastJSON2 解析
            return com.alibaba.fastjson2.JSON.parseObject(argumentsJson, 
                new com.alibaba.fastjson2.TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            // 如果解析失败，返回原始字符串作为单个参数
            return Map.of("raw", argumentsJson);
        }
    }
}
