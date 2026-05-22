package com.spintale.ai.providers.common.routing;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import jakarta.annotation.PostConstruct;

@Service
@ConditionalOnProperty(prefix = "spintale.ai.routing", name = "enabled", havingValue = "true")
public class LocalModelGateway {

    private final boolean enabled;
    private final String endpoint;
    private final String modelName;
    private final double temperature;
    private final long timeoutSeconds;

    private ChatModel chatModel;
    private EmbeddingModel embeddingModel;

    public LocalModelGateway(
            @Value("${spintale.ai.local-model.enabled:false}") boolean enabled,
            @Value("${spintale.ai.local-model.endpoint:http://localhost:11434}") String endpoint,
            @Value("${spintale.ai.local-model.model-name:qwen2.5:7b}") String modelName,
            @Value("${spintale.ai.local-model.temperature:0.7}") double temperature,
            @Value("${spintale.ai.local-model.timeout-seconds:30}") long timeoutSeconds) {
        this.enabled = enabled;
        this.endpoint = endpoint;
        this.modelName = modelName;
        this.temperature = temperature;
        this.timeoutSeconds = timeoutSeconds;
    }

    @PostConstruct
    public void initialize() {
        if (!enabled) {
            return;
        }
        this.chatModel = OpenAiChatModel.builder()
                .baseUrl(toOpenAiCompatibleBaseUrl(endpoint))
                .apiKey("ollama")
                .modelName(modelName)
                .temperature(temperature)
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .build();
    }

    public String getModelName() {
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
            return response == null || response.isBlank()
                    ? new HealthStatus(false, "NO_RESPONSE", "Local model returned an empty response")
                    : new HealthStatus(true, "HEALTHY", "Local model responded");
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
        return enabled;
    }

    public record HealthStatus(boolean healthy, String status, String message) {
    }
}
