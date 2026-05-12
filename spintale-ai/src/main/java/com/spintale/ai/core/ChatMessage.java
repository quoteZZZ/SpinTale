package com.spintale.ai.core;

public class ChatMessage
{
    private String role;
    private String content;
    private String toolCallId;
    private String toolName;

    public static ChatMessage system(String content) { return of("system", content); }
    public static ChatMessage user(String content) { return of("user", content); }
    public static ChatMessage assistant(String content) { return of("assistant", content); }

    public static ChatMessage of(String role, String content)
    {
        ChatMessage message = new ChatMessage();
        message.setRole(role);
        message.setContent(content);
        return message;
    }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getToolCallId() { return toolCallId; }
    public void setToolCallId(String toolCallId) { this.toolCallId = toolCallId; }
    public String getToolName() { return toolName; }
    public void setToolName(String toolName) { this.toolName = toolName; }
}
