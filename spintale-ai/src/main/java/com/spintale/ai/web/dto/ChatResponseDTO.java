package com.spintale.ai.web.dto;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 聊天响应 DTO
 */
public class ChatResponseDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 会话 ID
     */
    private String sessionId;

    /**
     * AI 回复内容
     */
    private String content;

    /**
     * 使用的模型
     */
    private String model;

    /**
     * Token 使用情况
     */
    private TokenUsageDTO tokenUsage;

    /**
     * 调用的工具列表
     */
    private List<ToolCallDTO> toolCalls;

    /**
     * 是否完成
     */
    private Boolean finished = true;

    /**
     * 额外数据
     */
    private Map<String, Object> extraData;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public TokenUsageDTO getTokenUsage() {
        return tokenUsage;
    }

    public void setTokenUsage(TokenUsageDTO tokenUsage) {
        this.tokenUsage = tokenUsage;
    }

    public List<ToolCallDTO> getToolCalls() {
        return toolCalls;
    }

    public void setToolCalls(List<ToolCallDTO> toolCalls) {
        this.toolCalls = toolCalls;
    }

    public Boolean getFinished() {
        return finished;
    }

    public void setFinished(Boolean finished) {
        this.finished = finished;
    }

    public Map<String, Object> getExtraData() {
        return extraData;
    }

    public void setExtraData(Map<String, Object> extraData) {
        this.extraData = extraData;
    }
}

/**
 * Token 使用情况 DTO
 */
class TokenUsageDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 输入 Token 数
     */
    private Integer inputTokens;

    /**
     * 输出 Token 数
     */
    private Integer outputTokens;

    /**
     * 总 Token 数
     */
    private Integer totalTokens;

    public TokenUsageDTO() {}

    public TokenUsageDTO(Integer inputTokens, Integer outputTokens, Integer totalTokens) {
        this.inputTokens = inputTokens;
        this.outputTokens = outputTokens;
        this.totalTokens = totalTokens;
    }

    public Integer getInputTokens() {
        return inputTokens;
    }

    public void setInputTokens(Integer inputTokens) {
        this.inputTokens = inputTokens;
    }

    public Integer getOutputTokens() {
        return outputTokens;
    }

    public void setOutputTokens(Integer outputTokens) {
        this.outputTokens = outputTokens;
    }

    public Integer getTotalTokens() {
        return totalTokens;
    }

    public void setTotalTokens(Integer totalTokens) {
        this.totalTokens = totalTokens;
    }
}

/**
 * 工具调用 DTO
 */
class ToolCallDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 工具 ID
     */
    private String id;

    /**
     * 工具名称
     */
    private String name;

    /**
     * 工具参数（JSON 字符串）
     */
    private String arguments;

    /**
     * 工具执行结果
     */
    private String result;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getArguments() {
        return arguments;
    }

    public void setArguments(String arguments) {
        this.arguments = arguments;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
