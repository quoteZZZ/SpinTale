package com.spintale.ai.api.advisor;

import com.spintale.ai.core.model.TokenUsage;

import java.util.HashMap;
import java.util.Map;

/**
 * Advisor 鍝嶅簲涓婁笅鏂?
 * 灏佽 LLM 杩斿洖鐨勭粨鏋滐紝鐢?Advisor 閾鹃?愭澶勭悊
 */
public class AdvisorResponse {

    /** AI 鍥炲鍐呭 */
    private String content;

    /** 浣跨敤鐨勬ā鍨嬪悕绉?*/
    private String model;

    /** Token 鐢ㄩ噺 */
    private TokenUsage tokenUsage;

    /** 浼氳瘽 ID */
    private String sessionId;

    /** 鏄惁瀹屾垚 */
    private boolean finished;

    /** 缃俊搴﹁瘎鍒嗭紙鐢卞够瑙夋娴?Advisor 濉厖锛?*/
    private Double confidenceScore;

    /** 鎵╁睍鏁版嵁 */
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
