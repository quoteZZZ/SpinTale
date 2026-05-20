package com.spintale.ai.infrastructure.autoconfig.provider;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.spintale.ai.core.provider.AiModelProvider;
import com.spintale.ai.infrastructure.properties.AiProperties;
import com.spintale.ai.infrastructure.provider.LangChain4jModelProvider;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;

@Configuration
public class OpenAiProviderAutoConfig
{
    private final AiProperties properties;

    public OpenAiProviderAutoConfig(AiProperties properties)
    {
        this.properties = properties;
    }

    @Bean(name = "openAiChatModel")
    @ConditionalOnMissingBean(ChatModel.class)
    @ConditionalOnProperty(prefix = "spintale.ai", name = "provider", havingValue = "openai", matchIfMissing = true)
    @ConditionalOnExpression("'${spintale.ai.openai.api-key:}' != ''")
    public ChatModel openAiChatModel()
    {
        var config = properties.getOpenai();
        return OpenAiChatModel.builder()
                .baseUrl(config.getBaseUrl())
                .apiKey(config.getApiKey())
                .modelName(properties.getModel())
                .timeout(Duration.ofMillis(config.getTimeout()))
                .build();
    }

    @Bean(name = "openAiCompatibleChatModel")
    @ConditionalOnMissingBean(ChatModel.class)
    @ConditionalOnProperty(prefix = "spintale.ai.openai-compatible", name = "enabled", havingValue = "true")
    public ChatModel openAiCompatibleChatModel()
    {
        var config = properties.getOpenaiCompatible();
        return OpenAiChatModel.builder()
                .baseUrl(config.getBaseUrl())
                .apiKey(config.getApiKey())
                .modelName(config.getModelName())
                .timeout(Duration.ofMillis(config.getTimeout()))
                .temperature(config.getTemperature())
                .maxTokens(config.getMaxTokens())
                .build();
    }

    @Bean
    @ConditionalOnMissingBean(name = "langChain4jOpenAiProvider")
    @ConditionalOnBean(name = "openAiChatModel")
    @ConditionalOnProperty(prefix = "spintale.ai", name = "provider", havingValue = "openai", matchIfMissing = true)
    public AiModelProvider langChain4jOpenAiProvider(@Qualifier("openAiChatModel") ChatModel openAiChatModel)
    {
        return new LangChain4jModelProvider("openai", AiModelProvider.Type.LANGCHAIN4J, openAiChatModel);
    }

    @Bean
    @ConditionalOnMissingBean(name = "langChain4jOpenAiCompatibleProvider")
    @ConditionalOnBean(name = "openAiCompatibleChatModel")
    public AiModelProvider langChain4jOpenAiCompatibleProvider(
            @Qualifier("openAiCompatibleChatModel") ChatModel openAiCompatibleChatModel)
    {
        return new LangChain4jModelProvider(properties.getProvider(), AiModelProvider.Type.OPENAI_COMPATIBLE, openAiCompatibleChatModel);
    }
}
