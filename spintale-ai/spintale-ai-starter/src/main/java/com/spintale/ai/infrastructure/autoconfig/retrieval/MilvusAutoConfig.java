package com.spintale.ai.infrastructure.autoconfig.retrieval;

import com.spintale.ai.infrastructure.properties.AiProperties;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Milvus vector store configuration for RAG.
 */
@Configuration
@ConditionalOnProperty(prefix = "spintale.ai.rag", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(AiProperties.class)
public class MilvusAutoConfig {

    @Bean(name = {"ragEmbeddingStore", "milvusEmbeddingStore"})
    @ConditionalOnClass(name = "dev.langchain4j.store.embedding.milvus.MilvusEmbeddingStore")
    @ConditionalOnMissingBean(name = "ragEmbeddingStore")
    @ConditionalOnProperty(prefix = "spintale.ai.rag", name = "vector-store", havingValue = "milvus", matchIfMissing = true)
    public EmbeddingStore<TextSegment> milvusEmbeddingStore(
            AiProperties properties) {
        
        try {
            Class<?> milvusClass = Class.forName("dev.langchain4j.store.embedding.milvus.MilvusEmbeddingStore");
            
            var milvus = properties.getRag().getMilvus();
            
            Object builder = milvusClass.getMethod("builder").invoke(null);
            
            builder.getClass().getMethod("uri", String.class).invoke(builder, milvus.getUri());
            builder.getClass().getMethod("collectionName", String.class).invoke(builder, milvus.getCollectionName());
            
            if (milvus.getUsername() != null && !milvus.getUsername().isEmpty()) {
                builder.getClass().getMethod("username", String.class).invoke(builder, milvus.getUsername());
            }
            if (milvus.getPassword() != null && !milvus.getPassword().isEmpty()) {
                builder.getClass().getMethod("password", String.class).invoke(builder, milvus.getPassword());
            }
            return (EmbeddingStore<TextSegment>) builder.getClass().getMethod("build").invoke(builder);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create MilvusEmbeddingStore", e);
        }
    }
}
