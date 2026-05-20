package com.spintale.ai.infrastructure.autoconfig.provider;

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
@ConditionalOnProperty(prefix = "spintale.ai", name = "provider", havingValue = "ollama")
public class OllamaProviderAutoConfig
{
    private final AiProperties properties;

    public OllamaProviderAutoConfig(AiProperties properties)
    {
        this.properties = properties;
    }

    @Bean(name = "ollamaChatModel")
    @ConditionalOnMissingBean(ChatModel.class)
    public ChatModel ollamaChatModel()
    {
        var config = properties.getOllama();
        return OpenAiChatModel.builder()
                .baseUrl(toOpenAiCompatibleBaseUrl(config.getBaseUrl()))
                .apiKey("ollama")
                .modelName(config.getModel())
                .build();
    }

    @Bean
    @ConditionalOnMissingBean(name = "langChain4jOllamaProvider")
    public AiModelProvider langChain4jOllamaProvider(ChatModel ollamaChatModel)
    {
        return new LangChain4jModelProvider("ollama", AiModelProvider.Type.OPENAI_COMPATIBLE, ollamaChatModel);
    }

    private String toOpenAiCompatibleBaseUrl(String baseUrl)
    {
        if (baseUrl == null || baseUrl.isBlank()) {
            return "http://localhost:11434/v1";
        }
        String normalized = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        return normalized.endsWith("/v1") ? normalized : normalized + "/v1";
    }
}
