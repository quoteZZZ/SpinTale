package com.spintale.ai.provider.catalog;

import java.time.Instant;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ModelCatalog
{
    private String modelId;
    private String modelName;
    private String provider;
    private String modelType;
    private ModelCapability capability;
    private ModelPrice pricing;
    private Map<String, Object> metadata;
    private boolean enabled;
    private Instant createdAt;
    private Instant updatedAt;

    public enum ModelType
    {
        CHAT,
        EMBEDDING,
        RERANK,
        IMAGE_GENERATION,
        AUDIO,
        MULTIMODAL
    }

    public boolean isChatModel()
    {
        return capability != null && capability.canChat();
    }

    public boolean isEmbeddingModel()
    {
        return capability != null && capability.canEmbed();
    }

    public boolean isRerankModel()
    {
        return capability != null && capability.canRerank();
    }

    public double estimateCost(int inputTokens, int outputTokens)
    {
        if (pricing == null)
        {
            return 0.0;
        }
        return pricing.calculateCost(inputTokens, outputTokens);
    }

    public static ModelCatalog of(String modelId, String modelName, 
            String provider, ModelCapability capability)
    {
        return ModelCatalog.builder()
                .modelId(modelId)
                .modelName(modelName)
                .provider(provider)
                .capability(capability)
                .enabled(true)
                .createdAt(Instant.now())
                .build();
    }
}
