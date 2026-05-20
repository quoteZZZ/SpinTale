package com.spintale.ai.api.provider;

import com.spintale.ai.api.provider.langchain4j.LangChain4jChatModelAdapter;
import com.spintale.ai.api.provider.langchain4j.LangChain4jStreamingChatModelAdapter;
import com.spintale.ai.core.constant.AiConstants;
import com.spintale.ai.core.spi.ChatModel;
import com.spintale.ai.core.spi.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;

/**
 * OpenAI Provider（基于抽象基类重构）
 */
public class OpenAiProviderV2 extends AbstractProvider<OpenAiProviderV2.OpenAiConfig> {

    private final String apiKey;

    public OpenAiProviderV2(OpenAiConfig config, String apiKey) {
        super(config);
        this.apiKey = Objects.requireNonNull(apiKey, "API key cannot be null");
    }

    @Override
    public ChatModel createChatModel() {
        OpenAiChatModel lcModel = OpenAiChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(config.getBaseUrl())
                .modelName(config.getModelName())
                .temperature(config.getTemperature())
                .maxTokens(config.getMaxTokens())
                .build();

        return new LangChain4jChatModelAdapter(
                AiConstants.PROVIDER_OPENAI,
                config.getModelName(),
                lcModel
        );
    }

    @Override
    public StreamingChatModel createStreamingChatModel() {
        OpenAiStreamingChatModel streamingModel = OpenAiStreamingChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(config.getBaseUrl())
                .modelName(config.getModelName())
                .temperature(config.getTemperature())
                .maxTokens(config.getMaxTokens())
                .build();

        return new LangChain4jStreamingChatModelAdapter(
                AiConstants.PROVIDER_OPENAI,
                config.getModelName(),
                streamingModel
        );
    }

    @Override
    public String getProviderName() {
        return AiConstants.PROVIDER_OPENAI;
    }

    import java.util.Objects;

    public static class OpenAiConfig extends ProviderConfig {
        public OpenAiConfig(String baseUrl, String modelName, 
                          Double temperature, Integer maxTokens, Integer timeout) {
            super(baseUrl != null ? baseUrl : "https://api.openai.com/v1",
                  modelName != null ? modelName : "gpt-4o-mini",
                  temperature, maxTokens, timeout);
        }
    }
}
