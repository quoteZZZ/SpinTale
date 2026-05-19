package com.spintale.ai.infrastructure.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
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
@ConditionalOnProperty(prefix = "spintale.ai.rag", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(AiProperties.class)
public class MilvusEmbeddingStoreConfig {

    /**
     * 注意：MilvusEmbeddingStore 需要额外的依赖支持
     * 如果需要使用 Milvus，请添加以下依赖到 pom.xml：
     * <dependency>
     *     <groupId>dev.langchain4j</groupId>
     *     <artifactId>langchain4j-milvus</artifactId>
     * </dependency>
     */
    @Bean
    @ConditionalOnClass(name = "dev.langchain4j.store.embedding.milvus.MilvusEmbeddingStore")
    public EmbeddingStore<TextSegment> milvusEmbeddingStore(
            EmbeddingModel embeddingModel,
            AiProperties properties) {
        
        try {
            // 动态加载 MilvusEmbeddingStore 类
            Class<?> milvusClass = Class.forName("dev.langchain4j.store.embedding.milvus.MilvusEmbeddingStore");
            
            AiProperties.MilvusConfig milvus = properties.getRag().getMilvus();
            
            // 使用反射创建 MilvusEmbeddingStore 实例
            Object builder = milvusClass.getMethod("builder").invoke(null);
            
            // 设置基本配置
            builder.getClass().getMethod("uri", String.class).invoke(builder, milvus.getUri());
            builder.getClass().getMethod("collectionName", String.class).invoke(builder, milvus.getCollectionName());
            
            // 可选的用户名和密码
            if (milvus.getUsername() != null && !milvus.getUsername().isEmpty()) {
                builder.getClass().getMethod("username", String.class).invoke(builder, milvus.getUsername());
            }
            if (milvus.getPassword() != null && !milvus.getPassword().isEmpty()) {
                builder.getClass().getMethod("password", String.class).invoke(builder, milvus.getPassword());
            }
            
            // 构建并返回实例
            return (EmbeddingStore<TextSegment>) builder.getClass().getMethod("build").invoke(builder);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create MilvusEmbeddingStore", e);
        }
    }
}
