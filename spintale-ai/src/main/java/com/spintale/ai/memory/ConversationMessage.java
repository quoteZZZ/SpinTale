package com.spintale.ai.memory;

import java.time.LocalDateTime;

public class ConversationMessage
{
    private String role;
    private String content;
    private LocalDateTime timestamp = LocalDateTime.now();
    private Integer tokenCount;

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public Integer getTokenCount() { return tokenCount; }
    public void setTokenCount(Integer tokenCount) { this.tokenCount = tokenCount; }
}
