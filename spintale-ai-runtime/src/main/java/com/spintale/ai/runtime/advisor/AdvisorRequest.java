package com.spintale.ai.runtime.advisor;

import com.spintale.ai.core.model.ChatMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Advisor 璇锋眰涓婁笅鏂?
 * 灏佽鍙戦?佺粰 LLM 鐨勬墍鏈変俊鎭紝鐢?Advisor 閾鹃?愭澧炲己
 */
public class AdvisorRequest {

    /** 鐢ㄦ埛娑堟伅 */
    private String userMessage;

    /** 绯荤粺鎻愮ず璇?*/
    private String systemPrompt;

    /** 瀵硅瘽鍘嗗彶 */
    private List<ChatMessage> history;

    /** 浼氳瘽 ID */
    private String sessionId;

    /** 鐢ㄦ埛 ID */
    private String userId;

    /** 娓╁害鍙傛暟 */
    private Double temperature;

    /** 鏈?澶?Token 鏁?*/
    private Integer maxTokens;

    /** 鏄惁娴佸紡 */
    private Boolean stream;

    /** 鎵╁睍鍙傛暟锛堢敤浜?Advisor 浼犻?掗澶栦俊鎭級 */
    private Map<String, Object> params;

    public AdvisorRequest() {
        this.history = new ArrayList<>();
        this.params = new HashMap<>();
    }

    /**
     * 浠?ChatRequest 鏋勯??
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
     * 鍒涘缓鍓湰锛堢敤浜?Advisor 閾句腑涓嶅彲鍙樹紶閫掞級
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
