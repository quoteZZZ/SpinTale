package com.spintale.ai.infrastructure.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.spintale.ai.infrastructure.provider.LangChainAiChatService;
import com.spintale.ai.core.service.AiChatService;
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
    @ConditionalOnProperty(prefix = "spintale.ai.openai", name = "api-key")
    public ChatModel openAiChatModel()
    {
        AiProperties.OpenAiConfig config = properties.getOpenai();
        return OpenAiChatModel.builder()
                .baseUrl(config.getBaseUrl())
                .apiKey(config.getApiKey())
                .modelName(properties.getModel())
                .timeout(java.time.Duration.ofMillis(config.getTimeout()))
                .build();
    }

    @Bean
    @ConditionalOnMissingBean(AiChatService.class)
    @ConditionalOnProperty(prefix = "spintale.ai.openai", name = "api-key")
    public AiChatService aiChatService(ChatModel chatModel)
    {
        return new LangChainAiChatService(chatModel);
    }
}
