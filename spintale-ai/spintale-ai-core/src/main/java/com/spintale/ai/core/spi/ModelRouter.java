package com.spintale.ai.core.spi;

import com.spintale.ai.core.exception.AiServiceException;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.function.Function;

/**
 * Intelligent model router that selects the best provider based on request characteristics.
 */
@Slf4j
public class ModelRouter {

    private final ModelProviderRegistry registry;
    private Function<String, Double> complexityEstimator;

    public ModelRouter(ModelProviderRegistry registry) {
        this.registry = registry;
        this.complexityEstimator = this::defaultComplexityEstimate;
    }

    /**
     * Route a request to the best provider.
     *
     * @param modelName requested model name (may be null)
     * @param complexity estimated task complexity (0.0 - 1.0)
     * @return selected provider
     */
    public ModelProvider route(String modelName, double complexity) {
        // If specific model requested, find providers that support it
        if (modelName != null && !modelName.isEmpty()) {
            List<ModelProvider> candidates = registry.findProvidersForModel(modelName);
            if (!candidates.isEmpty()) {
                log.debug("Routed to provider {} for model {}", 
                    candidates.get(0).getId(), modelName);
                return candidates.get(0);
            }
            log.warn("No provider supports model {}, falling back to default", modelName);
        }

        // Use complexity-based routing
        return routeByComplexity(complexity);
    }

    /**
     * Route based on task complexity.
     *
     * @param complexity task complexity (0.0 - 1.0)
     * @return selected provider
     */
    private ModelProvider routeByComplexity(double complexity) {
        List<ModelProvider> allProviders = registry.getAllProviders();
        
        if (allProviders.isEmpty()) {
            throw new AiServiceException("NO_PROVIDER_AVAILABLE", "No providers registered");
        }

        // Simple strategy: use default provider
        // Can be enhanced with cost/performance-based routing
        ModelProvider selected = registry.getDefaultProvider();
        log.debug("Routed by complexity {:.2f} to provider {}", 
            complexity, selected.getId());
        return selected;
    }

    /**
     * Estimate task complexity from input text.
     *
     * @param input user input text
     * @return complexity score (0.0 - 1.0)
     */
    public double estimateComplexity(String input) {
        if (input == null || input.isEmpty()) {
            return 0.0;
        }
        return complexityEstimator.apply(input);
    }

    /**
     * Set custom complexity estimation function.
     *
     * @param estimator the estimator function
     */
    public void setComplexityEstimator(Function<String, Double> estimator) {
        this.complexityEstimator = estimator != null ? estimator : this::defaultComplexityEstimate;
    }

    /**
     * Default complexity estimation based on simple heuristics.
     *
     * @param input input text
     * @return complexity score
     */
    private double defaultComplexityEstimate(String input) {
        int length = input.length();
        int wordCount = input.split("\\s+").length;
        
        // Simple heuristic: longer and more complex queries need better models
        double lengthScore = Math.min(length / 500.0, 1.0);
        double wordScore = Math.min(wordCount / 50.0, 1.0);
        
        return (lengthScore + wordScore) / 2.0;
    }
}
