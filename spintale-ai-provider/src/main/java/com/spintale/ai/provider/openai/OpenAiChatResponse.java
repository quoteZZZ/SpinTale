package com.spintale.ai.provider.openai;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OpenAiChatResponse
{
    private String id;
    private String object;
    private Long created;
    private String model;
    private List<Choice> choices;
    private Usage usage;
    private Error error;

    @Data
    @Builder
    public static class Choice
    {
        private Integer index;
        private Message message;
        private Delta delta;
        private String finishReason;
    }

    @Data
    @Builder
    public static class Message
    {
        private String role;
        private String content;
        private List<OpenAiChatRequest.ToolCall> toolCalls;
    }

    @Data
    @Builder
    public static class Delta
    {
        private String role;
        private String content;
        private List<OpenAiChatRequest.ToolCall> toolCalls;
    }

    @Data
    @Builder
    public static class Usage
    {
        private Integer promptTokens;
        private Integer completionTokens;
        private Integer totalTokens;

        public int getInputTokens()
        {
            return promptTokens != null ? promptTokens : 0;
        }

        public int getOutputTokens()
        {
            return completionTokens != null ? completionTokens : 0;
        }
    }

    @Data
    @Builder
    public static class Error
    {
        private String message;
        private String type;
        private String code;
    }

    public boolean isSuccess()
    {
        return error == null && choices != null && !choices.isEmpty();
    }

    public String getContent()
    {
        if (!isSuccess()) return null;
        Message msg = choices.get(0).getMessage();
        return msg != null ? msg.getContent() : null;
    }

    public int getInputTokens()
    {
        return usage != null ? usage.getInputTokens() : 0;
    }

    public int getOutputTokens()
    {
        return usage != null ? usage.getOutputTokens() : 0;
    }
}
