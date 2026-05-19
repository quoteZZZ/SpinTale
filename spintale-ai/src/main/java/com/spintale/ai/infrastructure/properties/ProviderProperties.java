package com.spintale.ai.infrastructure.properties;

import lombok.Data;

/**
 * Provider and routing related AI settings.
 */
public final class ProviderProperties {

    private ProviderProperties() {
    }

    @Data
    public static class OpenAiConfig {
        private String apiKey;
        private String baseUrl = "https://api.openai.com/v1";
        private Long timeout = 60000L;
    }

    @Data
    public static class OpenAiCompatibleConfig {
        private Boolean enabled = false;
        private String apiKey;
        private String baseUrl = "https://api.openai.com/v1";
        private String modelName = "gpt-4o-mini";
        private Long timeout = 60000L;
        private Double temperature = 0.7;
        private Integer maxTokens = 2048;
    }

    @Data
    public static class AzureOpenAiConfig {
        private String apiKey;
        private String endpoint;
        private String deploymentName;
        private String apiVersion = "2024-02-15-preview";
    }

    @Data
    public static class OllamaConfig {
        private String baseUrl = "http://localhost:11434";
        private String model = "llama3.1";
    }

    @Data
    public static class AnthropicConfig {
        private String apiKey;
        private String baseUrl = "https://api.anthropic.com";
        private String model = "claude-3-sonnet-20240229";
    }

    @Data
    public static class LocalModelConfig {
        private Boolean enabled = false;
        private String provider = "ollama";
        private String endpoint = "http://localhost:11434";
        private String modelName = "qwen2.5:7b";
        private Double temperature = 0.7;
        private Long timeoutSeconds = 30L;
    }

    @Data
    public static class RoutingConfig {
        private Boolean enabled = false;
        private Double complexityThreshold = 0.6;
    }
}
