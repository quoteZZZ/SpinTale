package com.spintale.ai.provider.openai;

import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OpenAiChatRequest
{
    private String model;
    private List<Message> messages;
    private Double temperature;
    private Integer maxTokens;
    private Boolean stream;
    private List<Tool> tools;
    private ToolChoice toolChoice;
    private Map<String, Object> metadata;

    @Data
    @Builder
    public static class Message
    {
        private String role;
        private String content;
        private List<ContentPart> contentParts;
        private String name;
        private String toolCallId;
        private List<ToolCall> toolCalls;
    }

    @Data
    @Builder
    public static class ContentPart
    {
        private String type;
        private String text;
        private ImageUrl imageUrl;
    }

    @Data
    @Builder
    public static class ImageUrl
    {
        private String url;
        private String detail;
    }

    @Data
    @Builder
    public static class Tool
    {
        private String type;
        private Function function;
    }

    @Data
    @Builder
    public static class Function
    {
        private String name;
        private String description;
        private Map<String, Object> parameters;
    }

    @Data
    @Builder
    public static class ToolCall
    {
        private String id;
        private String type;
        private FunctionCall function;
    }

    @Data
    @Builder
    public static class FunctionCall
    {
        private String name;
        private String arguments;
    }

    public record ToolChoice(String type, Function function) {}

    public static OpenAiChatRequest simple(String model, String systemPrompt, String userMessage)
    {
        return OpenAiChatRequest.builder()
                .model(model)
                .messages(List.of(
                        Message.builder().role("system").content(systemPrompt).build(),
                        Message.builder().role("user").content(userMessage).build()
                ))
                .build();
    }

    public static OpenAiChatRequest chat(String model, List<Message> messages)
    {
        return OpenAiChatRequest.builder()
                .model(model)
                .messages(messages)
                .build();
    }
}
