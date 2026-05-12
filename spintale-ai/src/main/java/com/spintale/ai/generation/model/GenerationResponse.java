package com.spintale.ai.generation.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import com.spintale.ai.core.TokenUsage;

/**
 * AI 内容生成响应
 */
public class GenerationResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 生成的内容
     */
    private String content;

    /**
     * 内容类型
     */
    private ContentType contentType;

    /**
     * 使用的模型
     */
    private String model;

    /**
     * Token 使用情况
     */
    private TokenUsage tokenUsage;

    /**
     * 生成时间
     */
    private LocalDateTime generatedAt;

    /**
     * 是否完成
     */
    private boolean completed;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 额外元数据
     */
    private Object metadata;

    public GenerationResponse() {
        this.generatedAt = LocalDateTime.now();
        this.completed = true;
    }

    public GenerationResponse(String content, ContentType contentType) {
        this();
        this.content = content;
        this.contentType = contentType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public ContentType getContentType() {
        return contentType;
    }

    public void setContentType(ContentType contentType) {
        this.contentType = contentType;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public TokenUsage getTokenUsage() {
        return tokenUsage;
    }

    public void setTokenUsage(TokenUsage tokenUsage) {
        this.tokenUsage = tokenUsage;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
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

    public Object getMetadata() {
        return metadata;
    }

    public void setMetadata(Object metadata) {
        this.metadata = metadata;
    }

    /**
     * 构建成功响应
     */
    public static GenerationResponse success(String content, ContentType contentType) {
        return new GenerationResponse(content, contentType);
    }

    /**
     * 构建错误响应
     */
    public static GenerationResponse error(String errorMessage) {
        GenerationResponse response = new GenerationResponse();
        response.setCompleted(false);
        response.setErrorMessage(errorMessage);
        return response;
    }
}
