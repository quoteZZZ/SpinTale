package com.spintale.ai.provider.catalog;

import java.util.Set;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ModelCapability
{
    private String modelId;
    private String modelName;
    private Set<Capability> capabilities;
    private int maxContextTokens;
    private int maxOutputTokens;
    private boolean supportsStreaming;
    private boolean supportsFunctionCalling;
    private boolean supportsVision;
    private boolean supportsJsonMode;
    private boolean supportsEmbedding;
    private boolean supportsRerank;

    public enum Capability
    {
        CHAT,
        STREAMING,
        FUNCTION_CALLING,
        VISION,
        JSON_MODE,
        EMBEDDING,
        RERANK,
        CODE_GENERATION,
        REASONING,
        MULTIMODAL
    }

    public boolean hasCapability(Capability capability)
    {
        return capabilities != null && capabilities.contains(capability);
    }

    public boolean canChat()
    {
        return hasCapability(Capability.CHAT);
    }

    public boolean canStream()
    {
        return supportsStreaming || hasCapability(Capability.STREAMING);
    }

    public boolean canCallFunctions()
    {
        return supportsFunctionCalling || hasCapability(Capability.FUNCTION_CALLING);
    }

    public boolean canSeeImages()
    {
        return supportsVision || hasCapability(Capability.VISION);
    }

    public boolean canEmbed()
    {
        return supportsEmbedding || hasCapability(Capability.EMBEDDING);
    }

    public boolean canRerank()
    {
        return supportsRerank || hasCapability(Capability.RERANK);
    }

    public static ModelCapability chatModel(String modelId, String modelName, int maxTokens)
    {
        return ModelCapability.builder()
                .modelId(modelId)
                .modelName(modelName)
                .maxContextTokens(maxTokens)
                .capabilities(Set.of(Capability.CHAT, Capability.STREAMING))
                .supportsStreaming(true)
                .build();
    }

    public static ModelCapability embeddingModel(String modelId, String modelName)
    {
        return ModelCapability.builder()
                .modelId(modelId)
                .modelName(modelName)
                .capabilities(Set.of(Capability.EMBEDDING))
                .supportsEmbedding(true)
                .build();
    }

    public static ModelCapability rerankModel(String modelId, String modelName)
    {
        return ModelCapability.builder()
                .modelId(modelId)
                .modelName(modelName)
                .capabilities(Set.of(Capability.RERANK))
                .supportsRerank(true)
                .build();
    }
}
