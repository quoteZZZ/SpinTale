package com.spintale.ai.api.provider.ollama;

import com.spintale.ai.api.provider.langchain4j.LangChain4jChatModelAdapter;
import com.spintale.ai.api.provider.langchain4j.LangChain4jStreamingChatModelAdapter;
import com.spintale.ai.core.constant.AiConstants;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
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
        OpenAiChatModel lcModel = OpenAiChatModel.builder()
            .apiKey("ollama")
            .baseUrl(baseUrl)
            .modelName(modelName)
            .temperature(temperature)
            .maxTokens(maxTokens)
            .build();

        return new LangChain4jChatModelAdapter(
            AiConstants.PROVIDER_OLLAMA,
            modelName,
            lcModel
        );
    }

    /**
     * Create a StreamingChatModel adapter for Ollama.
     * LangChain4j 1.13.1+ supports OpenAiStreamingChatModel for Ollama.
     *
     * @return StreamingChatModel instance
     */
    public com.spintale.ai.core.spi.StreamingChatModel createStreamingChatModel() {
        OpenAiStreamingChatModel streamingModel = OpenAiStreamingChatModel.builder()
            .apiKey("ollama")
            .baseUrl(baseUrl)
            .modelName(modelName)
            .temperature(temperature)
            .maxTokens(maxTokens)
            .build();

        return new LangChain4jStreamingChatModelAdapter(
            AiConstants.PROVIDER_OLLAMA,
            modelName,
            streamingModel
        );
    }
}
