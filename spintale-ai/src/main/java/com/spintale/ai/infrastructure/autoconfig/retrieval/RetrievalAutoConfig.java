package com.spintale.ai.infrastructure.autoconfig.retrieval;

import com.spintale.ai.infrastructure.properties.AiProperties;
import com.spintale.ai.infrastructure.properties.RagProperties;
import com.spintale.ai.retrieval.vector.RetrievalService;
import com.spintale.ai.retrieval.vector.VectorRetrievalService;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "spintale.ai.rag", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(AiProperties.class)
public class RetrievalAutoConfig {

    @Bean(name = "ragEmbeddingStore")
    @ConditionalOnProperty(prefix = "spintale.ai.rag", name = "vector-store", havingValue = "memory")
    @ConditionalOnMissingBean(name = "ragEmbeddingStore")
    public EmbeddingStore<TextSegment> ragEmbeddingStore() {
        return new InMemoryEmbeddingStore<>();
    }

    @Bean
    @ConditionalOnBean(value = EmbeddingModel.class, name = "ragEmbeddingStore")
    @ConditionalOnMissingBean(RetrievalService.class)
    public RetrievalService retrievalService(
            @Qualifier("ragEmbeddingStore") EmbeddingStore<TextSegment> embeddingStore,
            EmbeddingModel embeddingModel,
            AiProperties properties) {
        RagProperties rag = properties.getRag();
        return new VectorRetrievalService(
                embeddingStore,
                embeddingModel,
                rag.getMaxSegmentSize(),
                rag.getMaxOverlapSize());
    }
}
