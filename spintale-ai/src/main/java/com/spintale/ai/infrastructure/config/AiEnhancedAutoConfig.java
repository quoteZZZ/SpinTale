package com.spintale.ai.infrastructure.config;

import com.spintale.ai.core.service.AiChatService;
// TODO: EnhancedAiChatService 需要创建或恢复
// import com.spintale.ai.capability.EnhancedAiChatService;
import com.spintale.ai.capability.memory.ConversationManager;
import com.spintale.ai.capability.memory.InMemoryConversationManager;
import com.spintale.ai.capability.memory.InMemoryLongTermMemoryManager;
import com.spintale.ai.capability.memory.LongTermMemory;
import com.spintale.ai.capability.memory.LongTermMemoryManager;
import com.spintale.ai.capability.hallucination.HallucinationDetectionService;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AI 增强功能自动配置
 * 
 * 提供以下功能的自动配置：
 * 1. 长期记忆管理
 * 2. 幻觉检测服务
 * 3. 增强型聊天服务
 */
@Configuration
@EnableConfigurationProperties(AiProperties.class)
public class AiEnhancedAutoConfig {
    
    /**
     * 配置长期记忆管理器
     * 使用内存存储（生产环境建议替换为数据库 + 向量数据库）
     */
    @Bean
    @ConditionalOnMissingBean(LongTermMemoryManager.class)
    public LongTermMemoryManager longTermMemoryManager(
            EmbeddingStore<LongTermMemory> embeddingStore,
            EmbeddingModel embeddingModel,
            ChatModel chatModel) {
        return new InMemoryLongTermMemoryManager(embeddingStore, embeddingModel, chatModel);
    }
    
    /**
     * 配置记忆向量存储（内存版）
     * 生产环境建议使用专门的向量数据库如 Milvus、Pinecone、Weaviate 等
     */
    @Bean
    @ConditionalOnMissingBean(name = "memoryEmbeddingStore")
    public EmbeddingStore<LongTermMemory> memoryEmbeddingStore() {
        return new InMemoryEmbeddingStore<>();
    }
    
    /**
     * 配置对话会话管理器
     */
    @Bean
    @ConditionalOnMissingBean(ConversationManager.class)
    public ConversationManager conversationManager() {
        return new InMemoryConversationManager();
    }
    
    /**
     * 配置幻觉检测服务
     */
    @Bean
    @ConditionalOnMissingBean(HallucinationDetectionService.class)
    public HallucinationDetectionService hallucinationDetectionService(ChatModel chatModel) {
        return new HallucinationDetectionService(chatModel);
    }
    
    /**
     * 配置增强型 AI 聊天服务
     * 在原有服务基础上增加长期记忆和幻觉检测功能
     * TODO: EnhancedAiChatService 需要实现
     */
    // @Bean
    // @ConditionalOnMissingBean(name = "enhancedAiChatService")
    // public AiChatService enhancedAiChatService(
    //         AiChatService delegate,
    //         ConversationManager conversationManager,
    //         LongTermMemoryManager longTermMemoryManager,
    //         HallucinationDetectionService hallucinationDetectionService,
    //         ChatModel chatModel,
    //         AiProperties properties) {
    //     
    //     EnhancedAiChatService enhancedService = new EnhancedAiChatService(
    //             delegate,
    //             conversationManager,
    //             longTermMemoryManager,
    //             hallucinationDetectionService,
    //             chatModel);
    //     
    //     // 应用配置参数
    //     if (properties.getContext() != null) {
    //         Integer maxContextMessages = properties.getContext().getMaxMessages();
    //         if (maxContextMessages != null) {
    //             enhancedService.setMaxContextMessages(maxContextMessages);
    //         }
    //         
    //         Double memoryThreshold = properties.getContext().getMemoryRetrievalThreshold();
    //         if (memoryThreshold != null) {
    //             enhancedService.setMemoryRetrievalThreshold(memoryThreshold);
    //         }
    //     }
    //     
    //     Boolean hallucinationEnabled = properties.getHallucinationDetection().getEnabled();
    //     if (hallucinationEnabled != null) {
    //         enhancedService.setHallucinationDetectionEnabled(hallucinationEnabled);
    //     }
    //     
    //     return enhancedService;
    // }
}
