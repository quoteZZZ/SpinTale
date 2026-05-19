package com.spintale.ai.capability.advisor;

import com.spintale.ai.core.model.ChatMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Advisor 请求上下文
 * 封装发送给 LLM 的所有信息，由 Advisor 链逐步增强
 */
public class AdvisorRequest {

    /** 用户消息 */
    private String userMessage;

    /** 系统提示词 */
    private String systemPrompt;

    /** 对话历史 */
    private List<ChatMessage> history;

    /** 会话 ID */
    private String sessionId;

    /** 用户 ID */
    private String userId;

    /** 温度参数 */
    private Double temperature;

    /** 最大 Token 数 */
    private Integer maxTokens;

    /** 是否流式 */
    private Boolean stream;

    /** 扩展参数（用于 Advisor 传递额外信息） */
    private Map<String, Object> params;

    public AdvisorRequest() {
        this.history = new ArrayList<>();
        this.params = new HashMap<>();
    }

    /**
     * 从 ChatRequest 构造
     */
    public static AdvisorRequest from(String userMessage, String systemPrompt,
                                       List<ChatMessage> history, String sessionId,
                                       String userId, Double temperature,
                                       Integer maxTokens, Boolean stream) {
        AdvisorRequest request = new AdvisorRequest();
        request.setUserMessage(userMessage);
        request.setSystemPrompt(systemPrompt);
        request.setHistory(history != null ? new ArrayList<>(history) : new ArrayList<>());
        request.setSessionId(sessionId);
        request.setUserId(userId);
        request.setTemperature(temperature);
        request.setMaxTokens(maxTokens);
        request.setStream(stream);
        return request;
    }

    /**
     * 创建副本（用于 Advisor 链中不可变传递）
     */
    public AdvisorRequest copy() {
        AdvisorRequest copy = new AdvisorRequest();
        copy.userMessage = this.userMessage;
        copy.systemPrompt = this.systemPrompt;
        copy.history = new ArrayList<>(this.history);
        copy.sessionId = this.sessionId;
        copy.userId = this.userId;
        copy.temperature = this.temperature;
        copy.maxTokens = this.maxTokens;
        copy.stream = this.stream;
        copy.params = new HashMap<>(this.params);
        return copy;
    }

    // ==================== Getters & Setters ====================

    public String getUserMessage() { return userMessage; }
    public void setUserMessage(String userMessage) { this.userMessage = userMessage; }

    public String getSystemPrompt() { return systemPrompt; }
    public void setSystemPrompt(String systemPrompt) { this.systemPrompt = systemPrompt; }

    public List<ChatMessage> getHistory() { return history; }
    public void setHistory(List<ChatMessage> history) { this.history = history; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public Double getTemperature() { return temperature; }
    public void setTemperature(Double temperature) { this.temperature = temperature; }

    public Integer getMaxTokens() { return maxTokens; }
    public void setMaxTokens(Integer maxTokens) { this.maxTokens = maxTokens; }

    public Boolean getStream() { return stream; }
    public void setStream(Boolean stream) { this.stream = stream; }

    public Map<String, Object> getParams() { return params; }
    public void setParams(Map<String, Object> params) { this.params = params; }

    public Object getParam(String key) { return params.get(key); }
    public void setParam(String key, Object value) { params.put(key, value); }
}
