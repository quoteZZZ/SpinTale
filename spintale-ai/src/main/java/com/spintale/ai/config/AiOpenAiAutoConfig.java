package com.spintale.ai.config;

import com.spintale.ai.client.LangChainAiChatService;
import com.spintale.ai.core.AiChatService;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AI 自动配置 - OpenAI
 */
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "spintale.ai", name = "provider", havingValue = "openai")
public class AiOpenAiAutoConfig {
    
    private final AiProperties properties;
    
    @Bean
    @ConditionalOnProperty(prefix = "spintale.ai.openai", name = "api-key")
    public ChatLanguageModel openAiChatModel() {
        AiProperties.OpenAiConfig config = properties.getOpenai();
        
        return OpenAiChatModel.builder()
                .baseUrl(config.getBaseUrl())
                .apiKey(config.getApiKey())
                .modelName(properties.getModel())
                .timeout(config.getTimeout())
                .temperature(0.7)
                .build();
    }
    
    @Bean
    @ConditionalOnProperty(prefix = "spintale.ai.openai", name = "api-key")
    public AiChatService aiChatService(ChatLanguageModel chatLanguageModel) {
        return new LangChainAiChatService(chatLanguageModel);
    }
}
