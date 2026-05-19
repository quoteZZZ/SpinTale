package com.spintale.ai.infrastructure.autoconfig.capability;

import java.util.List;
import java.util.Locale;

import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.spintale.ai.capability.advisor.Advisor;
import com.spintale.ai.capability.advisor.AdvisorInterceptor;
import com.spintale.ai.capability.advisor.HallucinationAdvisor;
import com.spintale.ai.capability.advisor.MemoryAdvisor;
import com.spintale.ai.capability.advisor.ObservabilityAdvisor;
import com.spintale.ai.capability.advisor.RagAdvisor;
import com.spintale.ai.capability.advisor.SafetyAdvisor;
import com.spintale.ai.capability.advisor.SemanticCacheAdvisor;
import com.spintale.ai.capability.memory.ConversationManager;
import com.spintale.ai.capability.memory.ConversationCompressor;
import com.spintale.ai.capability.memory.InMemoryConversationManager;
import com.spintale.ai.capability.memory.InMemoryLongTermMemoryManager;
import com.spintale.ai.capability.memory.LongTermMemory;
import com.spintale.ai.capability.memory.LongTermMemoryManager;
import com.spintale.ai.capability.observability.CostMonitor;
import com.spintale.ai.infrastructure.properties.AiProperties;
import com.spintale.ai.retrieval.vector.RetrievalService;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import io.micrometer.core.instrument.MeterRegistry;

/**
 * Auto configuration for optional AI capabilities.
 */
@Configuration
@EnableConfigurationProperties(AiProperties.class)
public class CapabilityAutoConfig {

    @Bean
    @ConditionalOnMissingBean(name = "memoryEmbeddingStore")
    public EmbeddingStore<LongTermMemory> memoryEmbeddingStore() {
        return new InMemoryEmbeddingStore<>();
    }

    @Bean
    @ConditionalOnMissingBean(ConversationManager.class)
    public ConversationManager conversationManager() {
        return new InMemoryConversationManager();
    }

    @Bean
    @ConditionalOnBean({EmbeddingModel.class, ChatModel.class})
    @ConditionalOnMissingBean(LongTermMemoryManager.class)
    @ConditionalOnProperty(prefix = "spintale.ai.context", name = "long-term-memory-enabled", havingValue = "true", matchIfMissing = true)
    public LongTermMemoryManager longTermMemoryManager(
            EmbeddingStore<LongTermMemory> memoryEmbeddingStore,
            EmbeddingModel embeddingModel,
            ChatModel chatModel) {
        return new InMemoryLongTermMemoryManager(memoryEmbeddingStore, embeddingModel, chatModel);
    }

    @Bean
    @ConditionalOnBean(ChatModel.class)
    @ConditionalOnMissingBean(com.spintale.ai.capability.hallucination.HallucinationDetector.class)
    public com.spintale.ai.capability.hallucination.HallucinationDetector hallucinationDetector(
            ChatModel chatModel) {
        return new com.spintale.ai.capability.hallucination.HallucinationDetector(chatModel);
    }

    @Bean
    @ConditionalOnMissingBean(SafetyAdvisor.class)
    @ConditionalOnProperty(prefix = "spintale.ai.safety", name = "enabled", havingValue = "true", matchIfMissing = true)
    public SafetyAdvisor safetyAdvisor(AiProperties properties) {
        SafetyAdvisor advisor = new SafetyAdvisor();
        advisor.setSafetyLevel(parseEnum(
                SafetyAdvisor.SafetyLevel.class,
                properties.getSafety().getLevel(),
                SafetyAdvisor.SafetyLevel.MODERATE));
        return advisor;
    }

    @Bean
    @ConditionalOnBean({ConversationManager.class, LongTermMemoryManager.class})
    @ConditionalOnMissingBean(MemoryAdvisor.class)
    public MemoryAdvisor memoryAdvisor(ConversationManager conversationManager,
                                       LongTermMemoryManager longTermMemoryManager,
                                       ConversationCompressor conversationCompressor,
                                       AiProperties properties) {
        return new MemoryAdvisor(conversationManager, longTermMemoryManager, conversationCompressor)
                .setMaxContextMessages(properties.getContext().getMaxMessages())
                .setMemoryRetrievalThreshold(properties.getContext().getMemoryRetrievalThreshold())
                .setMaxRetrievedMemories(properties.getContext().getMaxRetrievedMemories());
    }

    @Bean
    @ConditionalOnBean(RetrievalService.class)
    @ConditionalOnMissingBean(RagAdvisor.class)
    @ConditionalOnProperty(prefix = "spintale.ai.rag", name = "enabled", havingValue = "true")
    public RagAdvisor ragAdvisor(RetrievalService retrievalService, AiProperties properties) {
        return new RagAdvisor(retrievalService)
                .setMaxRetrievedDocs(properties.getRag().getMaxRetrievedDocs())
                .setMinScore(properties.getRag().getMinScore())
                .setQueryRewritingEnabled(properties.getRag().getQueryRewritingEnabled());
    }

    @Bean
    @ConditionalOnMissingBean(HallucinationAdvisor.class)
    @ConditionalOnProperty(prefix = "spintale.ai.hallucination-detection", name = "enabled", havingValue = "true", matchIfMissing = true)
    public HallucinationAdvisor hallucinationAdvisor(AiProperties properties) {
        HallucinationAdvisor advisor = new HallucinationAdvisor()
                .setEnabled(properties.getHallucinationDetection().getEnabled())
                .setHallucinationThreshold(properties.getHallucinationDetection().getThreshold());
        advisor.setAction(parseEnum(
                HallucinationAdvisor.HallucinationAction.class,
                properties.getHallucinationDetection().getAction(),
                HallucinationAdvisor.HallucinationAction.WARN));
        return advisor;
    }

    @Bean
    @ConditionalOnBean({RedissonClient.class, EmbeddingModel.class})
    @ConditionalOnMissingBean(SemanticCacheAdvisor.class)
    @ConditionalOnProperty(prefix = "spintale.ai.semantic-cache", name = "enabled", havingValue = "true")
    public SemanticCacheAdvisor semanticCacheAdvisor(RedissonClient redissonClient,
                                                     EmbeddingModel embeddingModel,
                                                     AiProperties properties) {
        return new SemanticCacheAdvisor(redissonClient, embeddingModel)
                .setEnabled(properties.getSemanticCache().getEnabled())
                .setSimilarityThreshold(properties.getSemanticCache().getSimilarityThreshold())
                .setCacheTtlHours(properties.getSemanticCache().getTtlHours());
    }

    @Bean
    @ConditionalOnBean(MeterRegistry.class)
    @ConditionalOnMissingBean(CostMonitor.class)
    public CostMonitor costMonitor() {
        return new CostMonitor();
    }

    @Bean
    @ConditionalOnBean(MeterRegistry.class)
    @ConditionalOnMissingBean(ObservabilityAdvisor.class)
    public ObservabilityAdvisor observabilityAdvisor(
            MeterRegistry meterRegistry,
            ObjectProvider<CostMonitor> costMonitor) {
        return new ObservabilityAdvisor(meterRegistry, costMonitor.getIfAvailable());
    }

    @Bean
    @ConditionalOnMissingBean(AdvisorInterceptor.class)
    public AdvisorInterceptor advisorInterceptor(List<Advisor> advisors) {
        return new AdvisorInterceptor(advisors);
    }

    private static <T extends Enum<T>> T parseEnum(Class<T> enumType, String value, T defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Enum.valueOf(enumType, value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return defaultValue;
        }
    }
}
