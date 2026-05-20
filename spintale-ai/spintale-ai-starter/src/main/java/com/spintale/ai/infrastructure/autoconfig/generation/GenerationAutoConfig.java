package com.spintale.ai.infrastructure.autoconfig.generation;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.spintale.ai.core.service.AiChatService;
import com.spintale.ai.generation.service.ContentGenerationService;
import com.spintale.ai.generation.service.TextGenerationService;
import com.spintale.ai.generation.template.ContentTemplate;

/**
 * Auto configuration for content generation.
 */
@Configuration
@ConditionalOnClass(ContentGenerationService.class)
@ConditionalOnProperty(prefix = "spintale.ai.generation", name = "enabled", havingValue = "true", matchIfMissing = true)
public class GenerationAutoConfig {

    private static final Logger log = LoggerFactory.getLogger(GenerationAutoConfig.class);

    @Bean
    @ConditionalOnBean(AiChatService.class)
    @ConditionalOnMissingBean(TextGenerationService.class)
    public TextGenerationService textGenerationService(AiChatService aiChatService) {
        return new TextGenerationService(aiChatService);
    }

    @Bean
    @ConditionalOnBean(TextGenerationService.class)
    @ConditionalOnMissingBean(ContentGenerationService.class)
    public ContentGenerationService contentGenerationService(
            TextGenerationService textGenerationService,
            ObjectProvider<ContentTemplate> templatesProvider) {
        ContentGenerationService service = new ContentGenerationService(textGenerationService);
        List<ContentTemplate> templates = templatesProvider.orderedStream().toList();
        service.registerTemplates(templates);
        log.info("AI content generation initialized with {} templates", templates.size());
        return service;
    }
}
