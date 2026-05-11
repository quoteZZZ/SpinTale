package com.spintale.ai.config;

import com.spintale.ai.agent.ReActAgent;
import com.spintale.ai.core.AiChatService;
import com.spintale.ai.prompt.SimplePromptTemplate;
import com.spintale.ai.prompt.PromptTemplate;
import com.spintale.ai.retrieval.EmbeddingRetrievalService;
import com.spintale.ai.retrieval.RetrievalService;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * AI 模块自动配置 - 提供 Agent、RAG、Prompt 等核心能力
 */
@Configuration
@RequiredArgsConstructor
public class AiAutoConfig {
    
    private final AiProperties properties;
    
    /**
     * 提示词模板引擎
     */
    @Bean
    @ConditionalOnClass(SimplePromptTemplate.class)
    public PromptTemplate promptTemplate() {
        return new SimplePromptTemplate();
    }
    
    /**
     * RAG 检索服务（当存在 EmbeddingStore 和 EmbeddingModel 时启用）
     */
    @Bean
    @ConditionalOnClass({EmbeddingStore.class, EmbeddingModel.class})
    @ConditionalOnProperty(prefix = "spintale.ai.rag", name = "enabled", havingValue = "true")
    public RetrievalService retrievalService(
            EmbeddingStore<TextSegment> embeddingStore,
            EmbeddingModel embeddingModel) {
        return new EmbeddingRetrievalService(embeddingStore, embeddingModel);
    }
    
    /**
     * ReAct Agent（当存在 ChatLanguageModel 时启用）
     */
    @Bean
    @ConditionalOnClass(ChatLanguageModel.class)
    @ConditionalOnProperty(prefix = "spintale.ai.agent", name = "enabled", havingValue = "true", matchIfMissing = true)
    public ReActAgent reactAgent(
            ChatLanguageModel chatModel,
            Map<String, Function<Map<String, Object>, String>> toolFunctions,
            List<dev.langchain4j.agent.tool.ToolSpecification> toolSpecifications) {
        
        return new ReActAgent(chatModel, toolFunctions, toolSpecifications);
    }
    
    /**
     * 默认空工具函数映射（可被用户自定义 Bean 覆盖）
     */
    @Bean
    public Map<String, Function<Map<String, Object>, String>> toolFunctions() {
        return new HashMap<>();
    }
    
    /**
     * 默认空工具规范列表（可被用户自定义 Bean 覆盖）
     */
    @Bean
    public List<dev.langchain4j.agent.tool.ToolSpecification> toolSpecifications() {
        return List.of();
    }
}
