package com.spintale.ai.runtime.routing;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import com.spintale.ai.provider.catalog.ModelCatalog;
import com.spintale.ai.provider.catalog.ModelCapability;
import com.spintale.ai.provider.catalog.ProviderCatalog;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@Component
@RequiredArgsConstructor
public class ModelRouter
{
    private final Map<String, ModelCatalog> modelRegistry = new ConcurrentHashMap<>();
    private final Map<String, ProviderCatalog> providerRegistry = new ConcurrentHashMap<>();
    private RoutingStrategy defaultStrategy = RoutingStrategy.COST_OPTIMIZED;
    private Map<String, Integer> modelWeights = new HashMap<>();

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

    public void registerModel(ModelCatalog model)
    {
        modelRegistry.put(model.getModelId(), model);
    }

    public void registerProvider(ProviderCatalog provider)
    {
        providerRegistry.put(provider.getProviderId(), provider);
    }

    public RoutingDecision route(RoutingContext context)
    {
        if (context.getSpecifiedModel() != null)
        {
            return RoutingDecision.specified(
                    context.getSpecifiedModel(),
                    getProviderForModel(context.getSpecifiedModel())
            );
        }

        return switch (defaultStrategy)
        {
            case COST_OPTIMIZED -> routeByCost(context);
            case PERFORMANCE_OPTIMIZED -> routeByPerformance(context);
            case AVAILABILITY_BASED -> routeByAvailability(context);
            case CAPABILITY_MATCHED -> routeByCapability(context);
            case ROUND_ROBIN -> routeRoundRobin(context);
            case WEIGHTED -> routeByWeight(context);
            default -> routeByCost(context);
        };
    }

    private RoutingDecision routeByCost(RoutingContext context)
    {
        List<ModelCatalog> candidates = findCandidateModels(context);
        
        if (candidates.isEmpty())
        {
            return RoutingDecision.builder()
                    .reason("No available models found")
                    .build();
        }

        candidates.sort(Comparator.comparingDouble(this::estimateCost));

        ModelCatalog selected = candidates.get(0);
        List<String> fallbacks = candidates.stream()
                .skip(1)
                .map(ModelCatalog::getModelId)
                .toList();

        return RoutingDecision.builder()
                .selectedModel(selected.getModelId())
                .selectedProvider(selected.getProvider())
                .strategy(RoutingStrategy.COST_OPTIMIZED)
                .reason("Selected lowest cost model")
                .fallbackModels(fallbacks)
                .build();
    }

    private RoutingDecision routeByPerformance(RoutingContext context)
    {
        List<ModelCatalog> candidates = findCandidateModels(context);
        
        if (candidates.isEmpty())
        {
            return RoutingDecision.builder()
                    .reason("No available models found")
                    .build();
        }

        candidates.sort((a, b) -> {
            int aPerf = getPerformanceScore(a);
            int bPerf = getPerformanceScore(b);
            return Integer.compare(bPerf, aPerf);
        });

        ModelCatalog selected = candidates.get(0);
        return RoutingDecision.builder()
                .selectedModel(selected.getModelId())
                .selectedProvider(selected.getProvider())
                .strategy(RoutingStrategy.PERFORMANCE_OPTIMIZED)
                .score(getPerformanceScore(selected))
                .reason("Selected highest performance model")
                .build();
    }

    private RoutingDecision routeByAvailability(RoutingContext context)
    {
        List<ModelCatalog> candidates = findCandidateModels(context);
        
        for (ModelCatalog model : candidates)
        {
            ProviderCatalog provider = providerRegistry.get(model.getProvider());
            if (provider != null && provider.isAvailable())
            {
                return RoutingDecision.availabilityBased(
                        model.getModelId(),
                        model.getProvider()
                );
            }
        }

        return RoutingDecision.builder()
                .reason("No available providers found")
                .build();
    }

    private RoutingDecision routeByCapability(RoutingContext context)
    {
        List<ModelCatalog> candidates = findCandidateModels(context);
        
        List<ModelCatalog> matched = candidates.stream()
                .filter(m -> matchesCapabilities(m, context.getRequiredCapabilities()))
                .toList();

        if (matched.isEmpty())
        {
            return RoutingDecision.builder()
                    .reason("No models match required capabilities")
                    .build();
        }

        ModelCatalog selected = matched.get(0);
        return RoutingDecision.builder()
                .selectedModel(selected.getModelId())
                .selectedProvider(selected.getProvider())
                .strategy(RoutingStrategy.CAPABILITY_MATCHED)
                .reason("Selected model matching required capabilities")
                .build();
    }

    private RoutingDecision routeRoundRobin(RoutingContext context)
    {
        List<ModelCatalog> candidates = findCandidateModels(context);
        
        if (candidates.isEmpty())
        {
            return RoutingDecision.builder()
                    .reason("No available models found")
                    .build();
        }

        int index = (int) (System.currentTimeMillis() % candidates.size());
        ModelCatalog selected = candidates.get(index);
        
        return RoutingDecision.builder()
                .selectedModel(selected.getModelId())
                .selectedProvider(selected.getProvider())
                .strategy(RoutingStrategy.ROUND_ROBIN)
                .reason("Round-robin selection")
                .build();
    }

    private RoutingDecision routeByWeight(RoutingContext context)
    {
        List<ModelCatalog> candidates = findCandidateModels(context);
        
        if (candidates.isEmpty())
        {
            return RoutingDecision.builder()
                    .reason("No available models found")
                    .build();
        }

        int totalWeight = candidates.stream()
                .mapToInt(m -> modelWeights.getOrDefault(m.getModelId(), 1))
                .sum();

        int random = (int) (Math.random() * totalWeight);
        int cumulative = 0;
        
        for (ModelCatalog model : candidates)
        {
            cumulative += modelWeights.getOrDefault(model.getModelId(), 1);
            if (random < cumulative)
            {
                return RoutingDecision.builder()
                        .selectedModel(model.getModelId())
                        .selectedProvider(model.getProvider())
                        .strategy(RoutingStrategy.WEIGHTED)
                        .score(modelWeights.getOrDefault(model.getModelId(), 1))
                        .reason("Weighted random selection")
                        .build();
            }
        }

        ModelCatalog selected = candidates.get(0);
        return RoutingDecision.of(selected.getModelId(), selected.getProvider(), 
                RoutingStrategy.WEIGHTED);
    }

    private List<ModelCatalog> findCandidateModels(RoutingContext context)
    {
        return modelRegistry.values().stream()
                .filter(ModelCatalog::isEnabled)
                .filter(m -> matchesType(m, context.getModelType()))
                .filter(m -> matchesProvider(m, context.getPreferredProvider()))
                .toList();
    }

    private boolean matchesType(ModelCatalog model, String type)
    {
        if (type == null) return true;
        return type.equalsIgnoreCase(model.getModelType());
    }

    private boolean matchesProvider(ModelCatalog model, String preferredProvider)
    {
        if (preferredProvider == null) return true;
        return preferredProvider.equalsIgnoreCase(model.getProvider());
    }

    private boolean matchesCapabilities(ModelCatalog model, Set<ModelCapability.Capability> required)
    {
        if (required == null || required.isEmpty()) return true;
        ModelCapability cap = model.getCapability();
        if (cap == null) return false;
        return required.stream().allMatch(cap::hasCapability);
    }

    private double estimateCost(ModelCatalog model)
    {
        return model.getPricing() != null ? 
                model.getPricing().getInputPricePer1k() != null ? 
                        model.getPricing().getInputPricePer1k() : 0.0 : 0.0;
    }

    private int getPerformanceScore(ModelCatalog model)
    {
        if (model.getCapability() == null) return 0;
        int score = 0;
        if (model.getCapability().canStream()) score += 10;
        if (model.getCapability().canCallFunctions()) score += 20;
        if (model.getCapability().canSeeImages()) score += 15;
        return score;
    }

    private String getProviderForModel(String modelId)
    {
        ModelCatalog model = modelRegistry.get(modelId);
        return model != null ? model.getProvider() : null;
    }

    public void setModelWeight(String modelId, int weight)
    {
        modelWeights.put(modelId, weight);
    }

    public RoutingDecision route()
    {
        return route(RoutingContext.create());
    }

    public RoutingDecision route(String modelType)
    {
        return route(RoutingContext.create().withModelType(modelType));
    }
}
