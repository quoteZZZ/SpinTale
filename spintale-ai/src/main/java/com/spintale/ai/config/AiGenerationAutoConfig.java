package com.spintale.ai.config;

import com.spintale.ai.generation.service.ContentGenerationService;
import com.spintale.ai.generation.template.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.util.List;

/**
 * AI 内容生成自动配置
 */
@Configuration
@ConditionalOnClass(ContentGenerationService.class)
@ConditionalOnProperty(prefix = "spintale.ai.generation", name = "enabled", havingValue = "true", matchIfMissing = true)
public class AiGenerationAutoConfig {

    private static final Logger log = LoggerFactory.getLogger(AiGenerationAutoConfig.class);

    @Autowired
    private ContentGenerationService generationService;

    @Autowired(required = false)
    private List<ContentTemplate> templates;

    /**
     * 初始化时注册所有模板
     */
    @PostConstruct
    public void init() {
        if (templates != null && !templates.isEmpty()) {
            generationService.registerTemplates(templates);
            log.info("AI 内容生成模块初始化完成，已注册 {} 个内容模板", templates.size());
        } else {
            log.warn("未找到任何内容模板，请检查配置");
        }
    }
}
