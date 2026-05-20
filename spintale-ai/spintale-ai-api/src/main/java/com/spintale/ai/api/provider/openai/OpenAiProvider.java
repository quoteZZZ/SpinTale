package com.spintale.ai.api.provider.openai;

import com.spintale.ai.api.provider.langchain4j.LangChain4jChatModelAdapter;
import com.spintale.ai.core.constant.AiConstants;
import dev.langchain4j.model.openai.OpenAiChatModel;
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
        // Create LangChain4j OpenAI model
        OpenAiChatModel lcModel = OpenAiChatModel.builder()
            .apiKey(apiKey)
            .baseUrl(baseUrl)
            .modelName(modelName)
            .temperature(temperature)
            .maxTokens(maxTokens)
            .build();

        // Wrap with SpinTale adapter
        return new LangChain4jChatModelAdapter(
            AiConstants.PROVIDER_OPENAI,
            modelName,
            lcModel
        );
    }

    /**
     * Create a ChatModel adapter with streaming support.
     * Note: Streaming support requires LangChain4j 1.14+ with OpenAiStreamingChatModel.
     *
     * @return ChatModel instance (streaming not yet supported in current version)
     */
    public com.spintale.ai.core.spi.ChatModel createStreamingChatModel() {
        // TODO: Add streaming support when LangChain4j provides OpenAiStreamingChatModel
        // For now, return regular model
        return createChatModel();
    }
}
