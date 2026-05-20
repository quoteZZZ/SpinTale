package com.spintale.ai.client.provider.ollama;

import com.spintale.ai.client.provider.langchain4j.LangChain4jChatModelAdapter;
import com.spintale.ai.core.constant.AiConstants;
import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.Builder;

/**
 * Factory for creating Ollama ChatModel instances.
 * Ollama uses OpenAI-compatible API.
 */
public class OllamaProvider {

    private final String baseUrl;
    private final String modelName;
    private final Double temperature;
    private final Integer maxTokens;

    @Builder
    public OllamaProvider(String baseUrl, String modelName, 
                         Double temperature, Integer maxTokens) {
        this.baseUrl = baseUrl != null ? baseUrl : "http://localhost:11434/v1";
        this.modelName = modelName != null ? modelName : "llama3.1";
        this.temperature = temperature != null ? temperature : 0.7;
        this.maxTokens = maxTokens != null ? maxTokens : 2048;
    }

    /**
     * Create a ChatModel adapter for Ollama.
     *
     * @return ChatModel instance
     */
    public com.spintale.ai.core.spi.ChatModel createChatModel() {
        // Ollama uses OpenAI-compatible API
        OpenAiChatModel lcModel = OpenAiChatModel.builder()
            .apiKey("ollama")  // Ollama doesn't require API key
            .baseUrl(baseUrl)
            .modelName(modelName)
            .temperature(temperature)
            .maxTokens(maxTokens)
            .build();

        // Wrap with SpinTale adapter
        return new LangChain4jChatModelAdapter(
            AiConstants.PROVIDER_OLLAMA,
            modelName,
            lcModel
        );
    }

    /**
     * Create a ChatModel adapter with streaming support.
     * Note: Streaming support requires LangChain4j 1.14+.
     *
     * @return ChatModel instance (streaming not yet supported in current version)
     */
    public com.spintale.ai.core.spi.ChatModel createStreamingChatModel() {
        // TODO: Add streaming support when available
        return createChatModel();
    }
}
