package com.spintale.ai.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.milvus.MilvusEmbeddingStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Milvus 向量数据库自动配置
 * 
 * 当满足以下条件时自动配置：
 * 1. classpath 中存在 MilvusEmbeddingStore 类
 * 2. spintale.ai.rag.enabled=true
 * 3. spintale.ai.rag.vector-store=milvus
 */
@Configuration
@ConditionalOnClass(MilvusEmbeddingStore.class)
@ConditionalOnProperty(prefix = "spintale.ai.rag", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(AiProperties.class)
public class MilvusEmbeddingStoreConfig {

    @Bean
    public EmbeddingStore<TextSegment> milvusEmbeddingStore(
            EmbeddingModel embeddingModel,
            AiProperties properties) {
        
        AiProperties.MilvusConfig milvus = properties.getRag().getMilvus();
        
        MilvusEmbeddingStore.Builder builder = MilvusEmbeddingStore.builder()
            .uri(milvus.getUri())
            .collectionName(milvus.getCollectionName());
        
        // 可选的用户名和密码
        if (milvus.getUsername() != null && !milvus.getUsername().isEmpty()) {
            builder.username(milvus.getUsername());
        }
        if (milvus.getPassword() != null && !milvus.getPassword().isEmpty()) {
            builder.password(milvus.getPassword());
        }
        
        // 如果提供了 embedding 模型，自动设置 dimension
        if (embeddingModel != null) {
            // Milvus 会自动检测 dimension，这里可以不设置
            // builder.dimension(embeddingModel.embed("test").content().dimension());
        }
        
        return builder.build();
    }
}
