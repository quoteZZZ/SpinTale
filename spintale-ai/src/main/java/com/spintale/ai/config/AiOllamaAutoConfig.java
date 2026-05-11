package com.spintale.ai.config;

import com.spintale.ai.client.LangChainAiChatService;
import com.spintale.ai.core.AiChatService;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AI 自动配置 - Ollama (本地模型)
 */
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "spintale.ai", name = "provider", havingValue = "ollama")
public class AiOllamaAutoConfig {
    
    private final AiProperties properties;
    
    @Bean
    public ChatLanguageModel ollamaChatModel() {
        AiProperties.OllamaConfig config = properties.getOllama();
        
        return OllamaChatModel.builder()
                .baseUrl(config.getBaseUrl())
                .modelName(config.getModel())
                .temperature(0.7)
                .build();
    }
    
    @Bean
    public AiChatService aiChatService(ChatLanguageModel chatLanguageModel) {
        return new LangChainAiChatService(chatLanguageModel);
    }
}
