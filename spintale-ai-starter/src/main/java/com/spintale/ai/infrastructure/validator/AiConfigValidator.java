package com.spintale.ai.infrastructure.validator;

import com.spintale.ai.infrastructure.properties.AiProperties;
import com.spintale.ai.infrastructure.properties.ProviderProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AiConfigValidator implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger log = LoggerFactory.getLogger(AiConfigValidator.class);

    private final AiProperties aiProperties;

    public AiConfigValidator(AiProperties aiProperties) {
        this.aiProperties = aiProperties;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        if (!Boolean.TRUE.equals(aiProperties.getEnabled())) {
            log.info("AI module is disabled, skipping configuration validation");
            return;
        }

        List<String> warnings = new ArrayList<>();
        validateProviderConfig(warnings);
        validateMemoryConfig(warnings);
        validateRagConfig(warnings);

        if (warnings.isEmpty()) {
            log.info("AI configuration validation passed");
            return;
        }
        log.warn("AI configuration validation completed with {} warning(s)", warnings.size());
        warnings.forEach(warning -> log.warn("  - {}", warning));
    }

    private void validateProviderConfig(List<String> warnings) {
        String provider = aiProperties.getProvider();
        if (provider == null || provider.isBlank()) {
            warnings.add("spintale.ai.provider is not configured");
            return;
        }

        switch (provider.toLowerCase()) {
            case "openai" -> validateOpenAiConfig(warnings);
            case "openai-compatible" -> validateOpenAiCompatibleConfig(warnings);
            case "ollama" -> validateOllamaConfig(warnings);
            case "azure" -> validateAzureConfig(warnings);
            default -> warnings.add("Unknown AI provider: " + provider);
        }
    }

    private void validateOpenAiConfig(List<String> warnings) {
        ProviderProperties.OpenAiConfig openai = aiProperties.getOpenai();
        if (openai.getApiKey() == null || openai.getApiKey().isBlank()) {
            warnings.add("OpenAI API key is not configured");
        }
        if (aiProperties.getModel() == null || aiProperties.getModel().isBlank()) {
            warnings.add("OpenAI model is not configured");
        }
    }

    private void validateOpenAiCompatibleConfig(List<String> warnings) {
        ProviderProperties.OpenAiCompatibleConfig compat = aiProperties.getOpenaiCompatible();
        if (!Boolean.TRUE.equals(compat.getEnabled())) {
            warnings.add("OpenAI-compatible provider is selected but disabled");
        }
        if (compat.getApiKey() == null || compat.getApiKey().isBlank()) {
            warnings.add("OpenAI-compatible API key is not configured");
        }
        if (compat.getBaseUrl() == null || compat.getBaseUrl().isBlank()) {
            warnings.add("OpenAI-compatible base URL is not configured");
        }
    }

    private void validateOllamaConfig(List<String> warnings) {
        ProviderProperties.OllamaConfig ollama = aiProperties.getOllama();
        if (ollama.getBaseUrl() == null || ollama.getBaseUrl().isBlank()) {
            warnings.add("Ollama base URL is not configured");
        }
        if (ollama.getModel() == null || ollama.getModel().isBlank()) {
            warnings.add("Ollama model is not configured");
        }
    }

    private void validateAzureConfig(List<String> warnings) {
        ProviderProperties.AzureOpenAiConfig azure = aiProperties.getAzure();
        if (azure.getApiKey() == null || azure.getApiKey().isBlank()) {
            warnings.add("Azure OpenAI API key is not configured");
        }
        if (azure.getEndpoint() == null || azure.getEndpoint().isBlank()) {
            warnings.add("Azure OpenAI endpoint is not configured");
        }
        if (azure.getDeploymentName() == null || azure.getDeploymentName().isBlank()) {
            warnings.add("Azure OpenAI deployment name is not configured");
        }
    }

    private void validateMemoryConfig(List<String> warnings) {
        var session = aiProperties.getMemory().getSession();
        if (session.getExpireMinutes() == null || session.getExpireMinutes() <= 0) {
            warnings.add("Memory session expire time must be positive");
        }
        if (session.getMaxSize() == null || session.getMaxSize() <= 0) {
            warnings.add("Memory session max size must be positive");
        }
    }

    private void validateRagConfig(List<String> warnings) {
        var rag = aiProperties.getRag();
        if (!Boolean.TRUE.equals(rag.getEnabled())) {
            return;
        }
        if (rag.getVectorStore() == null || rag.getVectorStore().isBlank()) {
            warnings.add("RAG is enabled but vector store is not configured");
        }
    }
}
