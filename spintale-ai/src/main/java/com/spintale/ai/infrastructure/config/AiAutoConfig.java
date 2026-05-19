package com.spintale.ai.infrastructure.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.spintale.ai.prompt.PromptTemplate;
import com.spintale.ai.prompt.SimplePromptTemplate;
import dev.langchain4j.agent.tool.ToolSpecification;

@Configuration
public class AiAutoConfig
{
    @Bean
    @ConditionalOnMissingBean(PromptTemplate.class)
    public PromptTemplate promptTemplate()
    {
        return new SimplePromptTemplate();
    }

    @Bean
    @ConditionalOnMissingBean(name = "toolFunctions")
    public Map<String, Function<Map<String, Object>, String>> toolFunctions()
    {
        return new HashMap<>();
    }

    @Bean
    @ConditionalOnMissingBean(name = "toolSpecifications")
    public List<ToolSpecification> toolSpecifications()
    {
        return List.of();
    }
}
