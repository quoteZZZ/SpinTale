package com.spintale.ai.provider.ollama;

import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OllamaChatRequest
{
    private String model;
    private List<Message> messages;
    private Boolean stream;
    private String format;
    private Options options;
    private List<Tool> tools;

    @Data
    @Builder
    public static class Message
    {
        private String role;
        private String content;
        private List<Image> images;
    }

    @Data
    @Builder
    public static class Image
    {
        private String url;
    }

    @Data
    @Builder
    public static class Options
    {
        private Double temperature;
        private Integer numCtx;
        private Integer numPredict;
        private Integer topK;
        private Double topP;
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

    public static OllamaChatRequest simple(String model, String message)
    {
        return OllamaChatRequest.builder()
                .model(model)
                .messages(List.of(
                        Message.builder().role("user").content(message).build()
                ))
                .stream(false)
                .build();
    }

    public static OllamaChatRequest chat(String model, List<Message> messages)
    {
        return OllamaChatRequest.builder()
                .model(model)
                .messages(messages)
                .stream(false)
                .build();
    }
}
