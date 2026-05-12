package com.spintale.ai.core;

import java.util.List;
import java.util.Map;

public class ChatRequest
{
    private String sessionId;
    private String message;
    private String systemPrompt;
    private List<ChatMessage> history;
    private List<ChatMessage> messages;
    private Double temperature = 0.7D;
    private Integer maxTokens = 2048;
    private Boolean stream = false;
    private List<ToolDefinition> tools;
    private Map<String, Object> extraParams;

    public static Builder builder() { return new Builder(); }

    public static class Builder
    {
        private final ChatRequest request = new ChatRequest();
        public Builder sessionId(String value) { request.setSessionId(value); return this; }
        public Builder message(String value) { request.setMessage(value); return this; }
        public Builder systemPrompt(String value) { request.setSystemPrompt(value); return this; }
        public Builder history(List<ChatMessage> value) { request.setHistory(value); return this; }
        public Builder messages(List<ChatMessage> value) { request.setMessages(value); return this; }
        public Builder temperature(Double value) { request.setTemperature(value); return this; }
        public Builder maxTokens(Integer value) { request.setMaxTokens(value); return this; }
        public Builder stream(Boolean value) { request.setStream(value); return this; }
        public ChatRequest build() { return request; }
    }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getSystemPrompt() { return systemPrompt; }
    public void setSystemPrompt(String systemPrompt) { this.systemPrompt = systemPrompt; }
    public List<ChatMessage> getHistory() { return history; }
    public void setHistory(List<ChatMessage> history) { this.history = history; }
    public List<ChatMessage> getMessages() { return messages; }
    public void setMessages(List<ChatMessage> messages) { this.messages = messages; }
    public Double getTemperature() { return temperature; }
    public void setTemperature(Double temperature) { this.temperature = temperature; }
    public Integer getMaxTokens() { return maxTokens; }
    public void setMaxTokens(Integer maxTokens) { this.maxTokens = maxTokens; }
    public Boolean getStream() { return stream; }
    public void setStream(Boolean stream) { this.stream = stream; }
    public List<ToolDefinition> getTools() { return tools; }
    public void setTools(List<ToolDefinition> tools) { this.tools = tools; }
    public Map<String, Object> getExtraParams() { return extraParams; }
    public void setExtraParams(Map<String, Object> extraParams) { this.extraParams = extraParams; }
}
