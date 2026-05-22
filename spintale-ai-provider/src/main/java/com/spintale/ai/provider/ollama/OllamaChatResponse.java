package com.spintale.ai.provider.ollama;

import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OllamaChatResponse
{
    private String model;
    private Instant createdAt;
    private Message message;
    private Boolean done;
    private Long totalDuration;
    private Long loadDuration;
    private Integer promptEvalCount;
    private Long promptEvalDuration;
    private Integer evalCount;
    private Long evalDuration;

    @Data
    @Builder
    public static class Message
    {
        private String role;
        private String content;
        private List<ToolCall> toolCalls;
    }

    @Data
    @Builder
    public static class ToolCall
    {
        private String name;
        private Map<String, Object> arguments;
    }

    public String getContent()
    {
        return message != null ? message.getContent() : null;
    }

    public int getInputTokens()
    {
        return promptEvalCount != null ? promptEvalCount : 0;
    }

    public int getOutputTokens()
    {
        return evalCount != null ? evalCount : 0;
    }

    public boolean isSuccess()
    {
        return message != null && message.getContent() != null;
    }
}
