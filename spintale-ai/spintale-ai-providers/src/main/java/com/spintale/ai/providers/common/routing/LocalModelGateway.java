package com.spintale.ai.providers.common.routing;

import java.time.Duration;

import com.spintale.ai.infrastructure.properties.AiProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

/**
 * Manages an optional local or low-cost model endpoint.
 */
@Slf4j
@Service
@ConditionalOnProperty(prefix = "spintale.ai.routing", name = "enabled", havingValue = "true")
public class LocalModelGateway {

    private final AiProperties properties;

    private ChatModel chatModel;
    private EmbeddingModel embeddingModel;

    public LocalModelGateway(AiProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void initialize() {
        var config = properties.getLocalModel();
        boolean enabled = Boolean.TRUE.equals(config.getEnabled());
        if (!enabled) {
            log.debug("Local model routing is disabled");
            return;
        }

        String provider = config.getProvider();
        String normalizedProvider = provider == null ? "ollama" : provider.trim().toLowerCase();
        if (!normalizedProvider.equals("ollama")
                && !normalizedProvider.equals("vllm")
                && !normalizedProvider.equals("llama.cpp")) {
            log.warn("Unsupported local model provider '{}', using OpenAI-compatible mode", provider);
        }

        String modelName = config.getModelName() == null || config.getModelName().isBlank()
                ? "qwen2.5:7b"
                : config.getModelName();
        this.chatModel = OpenAiChatModel.builder()
                .baseUrl(toOpenAiCompatibleBaseUrl(config.getEndpoint()))
                .apiKey("ollama")
                .modelName(modelName)
                .temperature(config.getTemperature() == null ? 0.7 : config.getTemperature())
                .timeout(Duration.ofSeconds(config.getTimeoutSeconds() == null ? 30L : config.getTimeoutSeconds()))
                .build();
        log.info("Local model route initialized: provider={}, model={}, endpoint={}",
                provider, modelName, config.getEndpoint());
    }

    public String getModelName() {
        String modelName = properties.getLocalModel().getModelName();
        return modelName == null || modelName.isBlank() ? "local-model" : modelName;
    }

    public ChatModel getChatModel() {
        return isEnabled() ? chatModel : null;
    }

    public EmbeddingModel getEmbeddingModel() {
        return isEnabled() ? embeddingModel : null;
    }

    public boolean isAvailable() {
        return isEnabled() && chatModel != null;
    }

    public HealthStatus healthCheck() {
        if (!isEnabled()) {
            return new HealthStatus(false, "DISABLED", "Local model routing is disabled");
        }
        if (chatModel == null) {
            return new HealthStatus(false, "NOT_INITIALIZED", "Local model is not initialized");
        }
        try {
            String response = chatModel.chat("ping");
            if (response == null || response.isBlank()) {
                return new HealthStatus(false, "NO_RESPONSE", "Local model returned an empty response");
            }
            return new HealthStatus(true, "HEALTHY", "Local model responded");
        } catch (Exception e) {
            return new HealthStatus(false, "ERROR", e.getMessage());
        }
    }

    private String toOpenAiCompatibleBaseUrl(String value) {
        if (value == null || value.isBlank()) {
            return "http://localhost:11434/v1";
        }
        String normalized = value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
        return normalized.endsWith("/v1") ? normalized : normalized + "/v1";
    }

    private boolean isEnabled() {
        return Boolean.TRUE.equals(properties.getLocalModel().getEnabled());
    }

    public record HealthStatus(boolean healthy, String status, String message) {}
}
