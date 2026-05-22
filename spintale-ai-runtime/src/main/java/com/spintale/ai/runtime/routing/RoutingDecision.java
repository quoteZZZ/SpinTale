package com.spintale.ai.runtime.routing;

import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoutingDecision
{
    private String selectedModel;
    private String selectedProvider;
    private RoutingStrategy strategy;
    private double score;
    private String reason;
    private Map<String, Object> metadata;
    private List<String> fallbackModels;

    public enum RoutingStrategy
    {
        SPECIFIED,
        COST_OPTIMIZED,
        PERFORMANCE_OPTIMIZED,
        AVAILABILITY_BASED,
        ROUND_ROBIN,
        WEIGHTED,
        CAPABILITY_MATCHED
    }

    public boolean hasFallback()
    {
        return fallbackModels != null && !fallbackModels.isEmpty();
    }

    public String getNextFallback()
    {
        if (!hasFallback())
        {
            return null;
        }
        return fallbackModels.get(0);
    }

    public static RoutingDecision of(String model, String provider, RoutingStrategy strategy)
    {
        return RoutingDecision.builder()
                .selectedModel(model)
                .selectedProvider(provider)
                .strategy(strategy)
                .build();
    }

    public static RoutingDecision specified(String model, String provider)
    {
        return RoutingDecision.builder()
                .selectedModel(model)
                .selectedProvider(provider)
                .strategy(RoutingStrategy.SPECIFIED)
                .reason("Explicitly specified model")
                .build();
    }

    public static RoutingDecision costOptimized(String model, String provider, double cost)
    {
        return RoutingDecision.builder()
                .selectedModel(model)
                .selectedProvider(provider)
                .strategy(RoutingStrategy.COST_OPTIMIZED)
                .score(cost)
                .reason("Selected lowest cost model")
                .build();
    }

    public static RoutingDecision availabilityBased(String model, String provider)
    {
        return RoutingDecision.builder()
                .selectedModel(model)
                .selectedProvider(provider)
                .strategy(RoutingStrategy.AVAILABILITY_BASED)
                .reason("Selected based on availability")
                .build();
    }
}
