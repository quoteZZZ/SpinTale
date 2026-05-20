package com.spintale.ai.api.provider.openai;

import com.spintale.ai.api.provider.langchain4j.LangChain4jChatModelAdapter;
import com.spintale.ai.api.provider.langchain4j.LangChain4jStreamingChatModelAdapter;
import com.spintale.ai.core.constant.AiConstants;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.Builder;

/**
 * Factory for creating OpenAI ChatModel instances.
 */
public class OpenAiProvider {

    private final String apiKey;
    private final String baseUrl;
    private final String modelName;
    private final Double temperature;
    private final Integer maxTokens;

    @Builder
    public OpenAiProvider(String apiKey, String baseUrl, String modelName, 
                         Double temperature, Integer maxTokens) {
        this.apiKey = apiKey;
        this.baseUrl = baseUrl != null ? baseUrl : "https://api.openai.com/v1";
        this.modelName = modelName != null ? modelName : "gpt-4o-mini";
        this.temperature = temperature != null ? temperature : 0.7;
        this.maxTokens = maxTokens != null ? maxTokens : 2048;
    }

    /**
     * Create a ChatModel adapter for OpenAI.
     *
     * @return ChatModel instance
     */
    public com.spintale.ai.core.spi.ChatModel createChatModel() {
        OpenAiChatModel lcModel = OpenAiChatModel.builder()
            .apiKey(apiKey)
            .baseUrl(baseUrl)
            .modelName(modelName)
            .temperature(temperature)
            .maxTokens(maxTokens)
            .build();

        return new LangChain4jChatModelAdapter(
            AiConstants.PROVIDER_OPENAI,
            modelName,
            lcModel
        );
    }

    /**
     * Create a StreamingChatModel adapter for OpenAI.
     * LangChain4j 1.13.1+ provides OpenAiStreamingChatModel.
     *
     * @return StreamingChatModel instance
     */
    public com.spintale.ai.core.spi.StreamingChatModel createStreamingChatModel() {
        OpenAiStreamingChatModel streamingModel = OpenAiStreamingChatModel.builder()
            .apiKey(apiKey)
            .baseUrl(baseUrl)
            .modelName(modelName)
            .temperature(temperature)
            .maxTokens(maxTokens)
            .build();

        return new LangChain4jStreamingChatModelAdapter(
            AiConstants.PROVIDER_OPENAI,
            modelName,
            streamingModel
        );
    }
}
