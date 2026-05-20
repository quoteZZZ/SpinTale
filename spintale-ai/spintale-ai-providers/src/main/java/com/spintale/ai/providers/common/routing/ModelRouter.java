package com.spintale.ai.providers.common.routing;

import com.spintale.ai.infrastructure.properties.AiProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import dev.langchain4j.model.chat.ChatModel;
import lombok.extern.slf4j.Slf4j;

/**
 * Routes simple requests to an optional local model.
 */
@Slf4j
@Service
@ConditionalOnProperty(prefix = "spintale.ai.routing", name = "enabled", havingValue = "true")
public class ModelRouter {

    private final LocalModelGateway localModelGateway;
    private final AiProperties properties;

    public ModelRouter(LocalModelGateway localModelGateway, AiProperties properties) {
        this.localModelGateway = localModelGateway;
        this.properties = properties;
    }

    public RoutingResult route(String query) {
        double complexity = calculateComplexity(query);
        boolean localAvailable = localModelGateway.isAvailable();
        Double configuredThreshold = properties.getRouting().getComplexityThreshold();
        double threshold = configuredThreshold == null ? 0.6 : configuredThreshold;
        boolean useLocalModel = localAvailable && complexity < threshold;

        String selectedModel = useLocalModel ? localModelGateway.getModelName() : "default-provider";
        long estimatedLatency = useLocalModel ? 50 : 800;
        double estimatedCost = useLocalModel ? 0.001 : 0.02;

        log.debug("Model route: complexity={}, localAvailable={}, selected={}",
                complexity, localAvailable, selectedModel);
        return new RoutingResult(selectedModel, complexity, useLocalModel, estimatedLatency, estimatedCost);
    }

    private double calculateComplexity(String query) {
        if (query == null || query.isBlank()) {
            return 0.0;
        }

        double score = 0.0;
        score += Math.min(query.length() / 200.0, 1.0) * 0.3;
        score += containsAny(query, "if", "because", "compare", "analyze", "why", "how", "steps", "plan") ? 0.4 : 0.0;
        long questionMarks = query.chars().filter(ch -> ch == '?').count();
        score += Math.min(questionMarks / 4.0, 1.0) * 0.3;
        return Math.min(score, 1.0);
    }

    private boolean containsAny(String query, String... words) {
        String lower = query.toLowerCase();
        for (String word : words) {
            if (lower.contains(word)) {
                return true;
            }
        }
        return false;
    }

    public ChatModel getActualModel(RoutingResult result) {
        if (result != null && result.useLocalModel()) {
            return localModelGateway.getChatModel();
        }
        return null;
    }

    public record RoutingResult(
            String selectedModel,
            double complexity,
            boolean useLocalModel,
            long estimatedLatencyMs,
            double estimatedCostUsd) {}
}
