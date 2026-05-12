package com.spintale.ai.generation.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 文本生成请求模型
 */
public class TextGenerationRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * API Key（可选，用于覆盖默认配置）
     */
    private String apiKey;

    /**
     * 模型名称（可选，用于覆盖默认配置）
     */
    private String modelName;

    /**
     * 主题/标题
     */
    private String title;

    /**
     * 提示词
     */
    private String prompt;

    /**
     * 系统提示词
     */
    private String systemPrompt;

    /**
     * 目标受众
     */
    private String audience;

    /**
     * 语气风格
     */
    private String tone;

    /**
     * 语言
     */
    private String language;

    /**
     * 长度预设
     */
    private String lengthPreset;

    /**
     * 额外参数
     */
    private Map<String, Object> extraParams;

    public TextGenerationRequest() {
        this.extraParams = new HashMap<>();
        this.language = "zh";
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public void setSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }

    public String getAudience() {
        return audience;
    }

    public void setAudience(String audience) {
        this.audience = audience;
    }

    public String getTone() {
        return tone;
    }

    public void setTone(String tone) {
        this.tone = tone;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getLengthPreset() {
        return lengthPreset;
    }

    public void setLengthPreset(String lengthPreset) {
        this.lengthPreset = lengthPreset;
    }

    public Map<String, Object> getExtraParams() {
        return extraParams;
    }

    public void setExtraParams(Map<String, Object> extraParams) {
        this.extraParams = extraParams;
    }

    public TextGenerationRequest putExtraParam(String key, Object value) {
        if (this.extraParams == null) {
            this.extraParams = new HashMap<>();
        }
        this.extraParams.put(key, value);
        return this;
    }
}
