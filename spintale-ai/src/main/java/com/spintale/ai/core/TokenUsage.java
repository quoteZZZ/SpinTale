package com.spintale.ai.core;

public class TokenUsage
{
    private Integer promptTokens = 0;
    private Integer completionTokens = 0;
    private Integer totalTokens = 0;

    public static Builder builder() { return new Builder(); }

    public static class Builder
    {
        private final TokenUsage usage = new TokenUsage();
        public Builder promptTokens(Integer value) { usage.setPromptTokens(value); return this; }
        public Builder completionTokens(Integer value) { usage.setCompletionTokens(value); return this; }
        public Builder totalTokens(Integer value) { usage.setTotalTokens(value); return this; }
        public TokenUsage build() { return usage; }
    }

    public Integer getPromptTokens() { return promptTokens; }
    public void setPromptTokens(Integer promptTokens) { this.promptTokens = promptTokens; }
    public Integer getCompletionTokens() { return completionTokens; }
    public void setCompletionTokens(Integer completionTokens) { this.completionTokens = completionTokens; }
    public Integer getTotalTokens() { return totalTokens; }
    public void setTotalTokens(Integer totalTokens) { this.totalTokens = totalTokens; }
}
