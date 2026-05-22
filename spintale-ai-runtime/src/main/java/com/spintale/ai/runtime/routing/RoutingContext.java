package com.spintale.ai.runtime.routing;

import java.util.Set;
import lombok.Builder;
import lombok.Data;
import com.spintale.ai.provider.catalog.ModelCapability;

@Data
@Builder
public class RoutingContext
{
    private String specifiedModel;
    private String modelType;
    private String preferredProvider;
    private Set<ModelCapability.Capability> requiredCapabilities;
    private Double maxCostPer1k;
    private Integer minContextTokens;
    private boolean requireStreaming;
    private boolean requireFunctionCalling;
    private boolean requireVision;
    private String userId;
    private String sessionId;

    public static RoutingContext create()
    {
        return RoutingContext.builder().build();
    }

    public RoutingContext withModel(String model)
    {
        this.specifiedModel = model;
        return this;
    }

    public RoutingContext withType(String type)
    {
        this.modelType = type;
        return this;
    }

    public RoutingContext withProvider(String provider)
    {
        this.preferredProvider = provider;
        return this;
    }

    public RoutingContext withCapabilities(Set<ModelCapability.Capability> capabilities)
    {
        this.requiredCapabilities = capabilities;
        return this;
    }

    public RoutingContext withCapability(ModelCapability.Capability capability)
    {
        if (this.requiredCapabilities == null)
        {
            this.requiredCapabilities = new java.util.HashSet<>();
        }
        this.requiredCapabilities.add(capability);
        return this;
    }

    public RoutingContext withMaxCost(double maxCost)
    {
        this.maxCostPer1k = maxCost;
        return this;
    }

    public RoutingContext withMinContext(int minTokens)
    {
        this.minContextTokens = minTokens;
        return this;
    }

    public RoutingContext requireStreaming()
    {
        this.requireStreaming = true;
        return this.withCapability(ModelCapability.Capability.STREAMING);
    }

    public RoutingContext requireFunctionCalling()
    {
        this.requireFunctionCalling = true;
        return this.withCapability(ModelCapability.Capability.FUNCTION_CALLING);
    }

    public RoutingContext requireVision()
    {
        this.requireVision = true;
        return this.withCapability(ModelCapability.Capability.VISION);
    }

    public RoutingContext forUser(String userId)
    {
        this.userId = userId;
        return this;
    }

    public RoutingContext forSession(String sessionId)
    {
        this.sessionId = sessionId;
        return this;
    }

    public static RoutingContext forChat()
    {
        return create()
                .withType("CHAT")
                .withCapability(ModelCapability.Capability.CHAT);
    }

    public static RoutingContext forEmbedding()
    {
        return create()
                .withType("EMBEDDING")
                .withCapability(ModelCapability.Capability.EMBEDDING);
    }

    public static RoutingContext forRerank()
    {
        return create()
                .withType("RERANK")
                .withCapability(ModelCapability.Capability.RERANK);
    }

    public static RoutingContext forAgent()
    {
        return create()
                .withType("CHAT")
                .withCapability(ModelCapability.Capability.CHAT)
                .withCapability(ModelCapability.Capability.FUNCTION_CALLING);
    }
}
