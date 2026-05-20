package com.spintale.ai.api.advisor;

import com.spintale.ai.core.model.TokenUsage;

import java.util.HashMap;
import java.util.Map;

/**
 * Advisor 响应上下�?
 * 封装 LLM 返回的结果，�?Advisor 链逐步处理
 */
public class AdvisorResponse {

    /** AI 回复内容 */
    private String content;

    /** 使用的模型名�?*/
    private String model;

    /** Token 用量 */
    private TokenUsage tokenUsage;

    /** 会话 ID */
    private String sessionId;

    /** 是否完成 */
    private boolean finished;

    /** 置信度评分（由幻觉检�?Advisor 填充�?*/
    private Double confidenceScore;

    /** 扩展数据 */
    private Map<String, Object> metadata;

    public AdvisorResponse() {
        this.metadata = new HashMap<>();
        this.finished = true;
    }

    public static AdvisorResponse of(String content, String model, TokenUsage tokenUsage, String sessionId) {
        AdvisorResponse response = new AdvisorResponse();
        response.setContent(content);
        response.setModel(model);
        response.setTokenUsage(tokenUsage);
        response.setSessionId(sessionId);
        return response;
    }

    // ==================== Getters & Setters ====================

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public TokenUsage getTokenUsage() { return tokenUsage; }
    public void setTokenUsage(TokenUsage tokenUsage) { this.tokenUsage = tokenUsage; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public boolean isFinished() { return finished; }
    public void setFinished(boolean finished) { this.finished = finished; }

    public Double getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(Double confidenceScore) { this.confidenceScore = confidenceScore; }

    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }

    public Object getMetadata(String key) { return metadata.get(key); }
    public void setMetadata(String key, Object value) { metadata.put(key, value); }
}
