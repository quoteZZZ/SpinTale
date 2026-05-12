package com.spintale.ai.generation.model;

import java.io.Serializable;

/**
 * 文本生成响应模型
 */
public class TextGenerationResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 生成的内容
     */
    private String content;

    /**
     * 使用的模型
     */
    private String model;

    /**
     * 使用的 Token 数量
     */
    private Integer promptTokens;

    /**
     * 生成的 Token 数量
     */
    private Integer completionTokens;

    /**
     * 总 Token 数量
     */
    private Integer totalTokens;

    /**
     * 是否完成
     */
    private boolean completed;

    /**
     * 错误信息
     */
    private String errorMessage;

    public TextGenerationResponse() {
        this.completed = true;
    }

    public TextGenerationResponse(String content) {
        this();
        this.content = content;
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

    public Integer getPromptTokens() {
        return promptTokens;
    }

    public void setPromptTokens(Integer promptTokens) {
        this.promptTokens = promptTokens;
    }

    public Integer getCompletionTokens() {
        return completionTokens;
    }

    public void setCompletionTokens(Integer completionTokens) {
        this.completionTokens = completionTokens;
    }

    public Integer getTotalTokens() {
        return totalTokens;
    }

    public void setTotalTokens(Integer totalTokens) {
        this.totalTokens = totalTokens;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * 构建成功响应
     */
    public static TextGenerationResponse success(String content) {
        return new TextGenerationResponse(content);
    }

    /**
     * 构建成功响应（带模型信息）
     */
    public static TextGenerationResponse success(String content, String model) {
        TextGenerationResponse response = new TextGenerationResponse(content);
        response.setModel(model);
        return response;
    }

    /**
     * 构建错误响应
     */
    public static TextGenerationResponse error(String errorMessage) {
        TextGenerationResponse response = new TextGenerationResponse();
        response.setCompleted(false);
        response.setErrorMessage(errorMessage);
        return response;
    }
}
