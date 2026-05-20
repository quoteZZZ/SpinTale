package com.spintale.ai.api.provider;

import com.spintale.ai.core.spi.ChatModel;
import com.spintale.ai.core.spi.StreamingChatModel;

import java.util.Objects;

/**
 * Provider抽象基类
 * 提供通用配置验证和模型创建模板
 */
public abstract class AbstractProvider<C extends ProviderConfig> {

    protected final C config;

    protected AbstractProvider(C config) {
        this.config = Objects.requireNonNull(config, "Provider config cannot be null");
        validateConfig(config);
    }

    /**
     * 验证配置
     */
    protected void validateConfig(C config) {
        if (config.getBaseUrl() == null || config.getBaseUrl().isBlank()) {
            throw new IllegalArgumentException("Base URL cannot be null or empty");
        }
        if (config.getModelName() == null || config.getModelName().isBlank()) {
            throw new IllegalArgumentException("Model name cannot be null or empty");
        }
    }

    /**
     * 创建同步聊天模型
     */
    public abstract ChatModel createChatModel();

    /**
     * 创建流式聊天模型
     */
    public abstract StreamingChatModel createStreamingChatModel();

    /**
     * 获取Provider名称
     */
    public abstract String getProviderName();

    /**
     * 获取配置
     */
    public C getConfig() {
        return config;
    }

    /**
     * Provider配置基类
     */
    public static abstract class ProviderConfig {
        private final String baseUrl;
        private final String modelName;
        private final Double temperature;
        private final Integer maxTokens;
        private final Integer timeout;

        protected ProviderConfig(String baseUrl, String modelName, 
                               Double temperature, Integer maxTokens, Integer timeout) {
            this.baseUrl = baseUrl;
            this.modelName = modelName;
            this.temperature = temperature != null ? temperature : 0.7;
            this.maxTokens = maxTokens != null ? maxTokens : 2048;
            this.timeout = timeout != null ? timeout : 60000;
        }

        public String getBaseUrl() { return baseUrl; }
        public String getModelName() { return modelName; }
        public Double getTemperature() { return temperature; }
        public Integer getMaxTokens() { return maxTokens; }
        public Integer getTimeout() { return timeout; }
    }
}
