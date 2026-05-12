package com.spintale.ai.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.spintale.ai.client.LangChainAiChatService;
import com.spintale.ai.core.AiChatService;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;

@Configuration
public class AiOpenAiAutoConfig
{
    private final AiProperties properties;

    public AiOpenAiAutoConfig(AiProperties properties)
    {
        this.properties = properties;
    }

    @Bean
    @ConditionalOnMissingBean(ChatModel.class)
    @ConditionalOnProperty(prefix = "spintale.ai.openai-compatible", name = "api-key")
    public ChatModel openAiCompatibleChatModel()
    {
        AiProperties.OpenAiCompatibleConfig config = properties.getOpenaiCompatible();
        return OpenAiChatModel.builder()
                .baseUrl(config.getBaseUrl())
                .apiKey(config.getApiKey())
                .modelName(config.getModelName())
                .timeout(config.getTimeout())
                .temperature(config.getTemperature())
                .maxTokens(config.getMaxTokens())
                .build();
    }

    @Bean
    @ConditionalOnMissingBean(AiChatService.class)
    @ConditionalOnProperty(prefix = "spintale.ai.openai-compatible", name = "api-key")
    public AiChatService aiChatService(ChatModel chatModel)
    {
        return new LangChainAiChatService(chatModel);
    }
}
