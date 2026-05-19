package com.spintale.ai.infrastructure.autoconfig.core;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.spintale.ai.core.pipeline.AiChatInterceptor;
import com.spintale.ai.core.prompt.PromptTemplate;
import com.spintale.ai.core.prompt.SimplePromptTemplate;
import com.spintale.ai.core.provider.AiModelProvider;
import com.spintale.ai.core.provider.AiProviderRegistry;
import com.spintale.ai.core.service.AiChatService;
import com.spintale.ai.core.client.ChatClient;
import com.spintale.ai.core.client.DefaultChatClient;
import com.spintale.ai.infrastructure.properties.AiProperties;
import com.spintale.ai.infrastructure.provider.RoutingChatService;
import com.spintale.ai.tool.registry.ToolRegistry;

import dev.langchain4j.agent.tool.ToolSpecification;

@Configuration
public class CoreAutoConfig
{
    @Bean
    @ConditionalOnMissingBean(PromptTemplate.class)
    public PromptTemplate promptTemplate()
    {
        return new SimplePromptTemplate();
    }

    @Bean
    @ConditionalOnMissingBean(AiProviderRegistry.class)
    public AiProviderRegistry aiProviderRegistry(List<AiModelProvider> providers, AiProperties properties)
    {
        return new AiProviderRegistry(providers, properties.getProvider());
    }

    @Bean
    @ConditionalOnMissingBean(AiChatService.class)
    public AiChatService aiChatService(AiProviderRegistry providerRegistry,
                                       List<AiChatInterceptor> interceptors)
    {
        return new RoutingChatService(providerRegistry, interceptors);
    }

    @Bean
    @ConditionalOnMissingBean(ChatClient.class)
    public ChatClient chatClient(AiChatService aiChatService)
    {
        return new DefaultChatClient(aiChatService);
    }

    @Bean
    @ConditionalOnMissingBean(name = "toolFunctions")
    public Map<String, Function<Map<String, Object>, String>> toolFunctions(ToolRegistry toolRegistry)
    {
        Map<String, Function<Map<String, Object>, String>> functions = new LinkedHashMap<>();
        for (String toolName : toolRegistry.getToolNames()) {
            ToolRegistry.RegisteredTool tool = toolRegistry.getTool(toolName);
            functions.put(tool.name(), tool.executor());
        }
        return functions;
    }

    @Bean
    @ConditionalOnMissingBean(name = "toolSpecifications")
    public List<ToolSpecification> toolSpecifications(ToolRegistry toolRegistry)
    {
        return toolRegistry.getToolSpecifications();
    }
}
