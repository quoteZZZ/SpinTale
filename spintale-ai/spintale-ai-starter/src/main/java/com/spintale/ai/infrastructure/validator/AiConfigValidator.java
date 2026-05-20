package com.spintale.ai.infrastructure.validator;

import com.spintale.ai.infrastructure.properties.AiProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 * AI配置验证器
 * 在应用启动时验证AI配置的完整性和正确性
 */
@Component
public class AiConfigValidator implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger log = LoggerFactory.getLogger(AiConfigValidator.class);

    private final AiProperties aiProperties;

    public AiConfigValidator(AiProperties aiProperties) {
        this.aiProperties = aiProperties;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        if (!aiProperties.isEnabled()) {
            log.info("AI module is disabled, skipping configuration validation");
            return;
        }

        log.info("Validating AI configuration...");
        List<String> errors = new ArrayList<>();

        try {
            validateProviderConfig(errors);
            validateMemoryConfig(errors);
            validateRagConfig(errors);

            if (errors.isEmpty()) {
                log.info("AI configuration validation passed successfully");
            } else {
                log.warn("AI configuration validation completed with {} warning(s):", errors.size());
                errors.forEach(error -> log.warn("  - {}", error));
            }
        } catch (Exception e) {
            log.error("AI configuration validation failed", e);
            throw new IllegalStateException("Invalid AI configuration: " + e.getMessage(), e);
        }
    }

    private void validateProviderConfig(List<String> errors) {
        String provider = aiProperties.getProvider();
        if (provider == null || provider.isBlank()) {
            errors.add("AI provider is not configured");
            return;
        }

        log.info("AI Provider: {}", provider);

        switch (provider.toLowerCase()) {
            case "openai":
                validateOpenAiConfig(errors);
                break;
            case "openai-compatible":
                validateOpenAiCompatibleConfig(errors);
                break;
            case "ollama":
                validateOllamaConfig(errors);
                break;
            case "azure":
                validateAzureConfig(errors);
                break;
            default:
                errors.add("Unknown AI provider: " + provider);
        }
    }

    private void validateOpenAiConfig(List<String> errors) {
        AiProperties.OpenAiProperties openai = aiProperties.getOpenai();
        if (openai == null || !openai.isEnabled()) {
            errors.add("OpenAI provider is selected but not enabled");
            return;
        }

        if (openai.getApiKey() == null || openai.getApiKey().isBlank()) {
            errors.add("OpenAI API key is not configured (set OPENAI_API_KEY environment variable)");
        }

        if (openai.getModelName() == null || openai.getModelName().isBlank()) {
            errors.add("OpenAI model name is not configured");
        }
    }

    private void validateOpenAiCompatibleConfig(List<String> errors) {
        AiProperties.OpenAiCompatibleProperties compat = aiProperties.getOpenAiCompatible();
        if (compat == null || !compat.isEnabled()) {
            errors.add("OpenAI-compatible provider is selected but not enabled");
            return;
        }

        if (compat.getApiKey() == null || compat.getApiKey().isBlank()) {
            errors.add("OpenAI-compatible API key is not configured");
        }

        if (compat.getBaseUrl() == null || compat.getBaseUrl().isBlank()) {
            errors.add("OpenAI-compatible base URL is not configured");
        }

        if (compat.getModelName() == null || compat.getModelName().isBlank()) {
            errors.add("OpenAI-compatible model name is not configured");
        }
    }

    private void validateOllamaConfig(List<String> errors) {
        AiProperties.OllamaProperties ollama = aiProperties.getOllama();
        if (ollama == null || !ollama.isEnabled()) {
            errors.add("Ollama provider is selected but not enabled");
            return;
        }

        if (ollama.getBaseUrl() == null || ollama.getBaseUrl().isBlank()) {
            errors.add("Ollama base URL is not configured");
        }
    }

    private void validateAzureConfig(List<String> errors) {
        AiProperties.AzureProperties azure = aiProperties.getAzure();
        if (azure == null || !azure.isEnabled()) {
            errors.add("Azure provider is selected but not enabled");
            return;
        }

        if (azure.getApiKey() == null || azure.getApiKey().isBlank()) {
            errors.add("Azure OpenAI API key is not configured");
        }

        if (azure.getEndpoint() == null || azure.getEndpoint().isBlank()) {
            errors.add("Azure OpenAI endpoint is not configured");
        }
    }

    private void validateMemoryConfig(List<String> errors) {
        AiProperties.MemoryProperties memory = aiProperties.getMemory();
        if (memory == null) {
            return;
        }

        AiProperties.SessionMemoryProperties session = memory.getSession();
        if (session != null && session.isEnabled()) {
            if (session.getExpireMinutes() <= 0) {
                errors.add("Memory session expire time must be positive");
            }
            if (session.getMaxSize() <= 0) {
                errors.add("Memory session max size must be positive");
            }
        }
    }

    private void validateRagConfig(List<String> errors) {
        AiProperties.RagProperties rag = aiProperties.getRag();
        if (rag == null || !rag.isEnabled()) {
            return;
        }

        if (rag.getVectorStore() == null || rag.getVectorStore().isBlank()) {
            errors.add("RAG is enabled but vector store is not configured");
        }
    }
}
