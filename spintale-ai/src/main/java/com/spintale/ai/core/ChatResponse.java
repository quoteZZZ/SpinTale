package com.spintale.ai.core;

import java.util.List;
import java.util.Map;

public class ChatResponse
{
    private String sessionId;
    private String content;
    private String model;
    private TokenUsage tokenUsage;
    private List<ToolCall> toolCalls;
    private Boolean finished = true;
    private Map<String, Object> extraData;

    public static Builder builder() { return new Builder(); }

    public static class Builder
    {
        private final ChatResponse response = new ChatResponse();
        public Builder sessionId(String value) { response.setSessionId(value); return this; }
        public Builder content(String value) { response.setContent(value); return this; }
        public Builder model(String value) { response.setModel(value); return this; }
        public Builder tokenUsage(TokenUsage value) { response.setTokenUsage(value); return this; }
        public Builder finished(Boolean value) { response.setFinished(value); return this; }
        public ChatResponse build() { return response; }
    }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public TokenUsage getTokenUsage() { return tokenUsage; }
    public void setTokenUsage(TokenUsage tokenUsage) { this.tokenUsage = tokenUsage; }
    public List<ToolCall> getToolCalls() { return toolCalls; }
    public void setToolCalls(List<ToolCall> toolCalls) { this.toolCalls = toolCalls; }
    public Boolean getFinished() { return finished; }
    public void setFinished(Boolean finished) { this.finished = finished; }
    public Map<String, Object> getExtraData() { return extraData; }
    public void setExtraData(Map<String, Object> extraData) { this.extraData = extraData; }
}
