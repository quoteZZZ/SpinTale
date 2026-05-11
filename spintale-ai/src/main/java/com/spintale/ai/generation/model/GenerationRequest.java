package com.spintale.ai.generation.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * AI 内容生成请求
 */
public class GenerationRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 内容类型
     */
    private ContentType contentType;

    /**
     * 主题/标题
     */
    private String title;

    /**
     * 关键词/要点
     */
    private String keywords;

    /**
     * 详细描述/要求
     */
    private String description;

    /**
     * 目标受众
     */
    private String targetAudience;

    /**
     * 语气风格 (正式、轻松、幽默、专业等)
     */
    private String tone;

    /**
     * 期望长度 (short, medium, long)
     */
    private String length;

    /**
     * 语言 (zh, en, etc.)
     */
    private String language;

    /**
     * 额外参数
     */
    private Map<String, Object> extraParams;

    /**
     * 是否流式输出
     */
    private boolean stream;

    public GenerationRequest() {
        this.extraParams = new HashMap<>();
        this.language = "zh";
        this.stream = false;
    }

    public ContentType getContentType() {
        return contentType;
    }

    public void setContentType(ContentType contentType) {
        this.contentType = contentType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTargetAudience() {
        return targetAudience;
    }

    public void setTargetAudience(String targetAudience) {
        this.targetAudience = targetAudience;
    }

    public String getTone() {
        return tone;
    }

    public void setTone(String tone) {
        this.tone = tone;
    }

    public String getLength() {
        return length;
    }

    public void setLength(String length) {
        this.length = length;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Map<String, Object> getExtraParams() {
        return extraParams;
    }

    public void setExtraParams(Map<String, Object> extraParams) {
        this.extraParams = extraParams;
    }

    public GenerationRequest putExtraParam(String key, Object value) {
        if (this.extraParams == null) {
            this.extraParams = new HashMap<>();
        }
        this.extraParams.put(key, value);
        return this;
    }

    public boolean isStream() {
        return stream;
    }

    public void setStream(boolean stream) {
        this.stream = stream;
    }

    /**
     * 构建器模式
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final GenerationRequest request = new GenerationRequest();

        public Builder contentType(ContentType contentType) {
            request.setContentType(contentType);
            return this;
        }

        public Builder title(String title) {
            request.setTitle(title);
            return this;
        }

        public Builder keywords(String keywords) {
            request.setKeywords(keywords);
            return this;
        }

        public Builder description(String description) {
            request.setDescription(description);
            return this;
        }

        public Builder targetAudience(String targetAudience) {
            request.setTargetAudience(targetAudience);
            return this;
        }

        public Builder tone(String tone) {
            request.setTone(tone);
            return this;
        }

        public Builder length(String length) {
            request.setLength(length);
            return this;
        }

        public Builder language(String language) {
            request.setLanguage(language);
            return this;
        }

        public Builder stream(boolean stream) {
            request.setStream(stream);
            return this;
        }

        public Builder extraParam(String key, Object value) {
            request.putExtraParam(key, value);
            return this;
        }

        public GenerationRequest build() {
            return request;
        }
    }
}
