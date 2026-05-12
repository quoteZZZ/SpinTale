package com.spintale.ai.web.dto;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 聊天请求 DTO
 */
public class ChatRequestDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 会话 ID（可选，不提供则自动创建）
     */
    private String sessionId;

    /**
     * 用户消息
     */
    private String message;

    /**
     * 系统提示词（可选）
     */
    private String systemPrompt;

    /**
     * 温度参数（0.0-2.0，默认 0.7）
     */
    private Double temperature = 0.7;

    /**
     * 最大 Token 数（默认 2048）
     */
    private Integer maxTokens = 2048;

    /**
     * 是否流式输出（默认 false）
     */
    private Boolean stream = false;

    /**
     * 启用的工具列表（可选）
     */
    private List<String> enabledTools;

    /**
     * 额外参数
     */
    private Map<String, Object> extraParams;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public void setSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Integer getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
    }

    public Boolean getStream() {
        return stream;
    }

    public void setStream(Boolean stream) {
        this.stream = stream;
    }

    public List<String> getEnabledTools() {
        return enabledTools;
    }

    public void setEnabledTools(List<String> enabledTools) {
        this.enabledTools = enabledTools;
    }

    public Map<String, Object> getExtraParams() {
        return extraParams;
    }

    public void setExtraParams(Map<String, Object> extraParams) {
        this.extraParams = extraParams;
    }
}
