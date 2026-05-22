package com.spintale.ai.providers.common.routing;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import dev.langchain4j.model.chat.ChatModel;

@Service
@ConditionalOnProperty(prefix = "spintale.ai.routing", name = "enabled", havingValue = "true")
public class ModelRouter {

    private final LocalModelGateway localModelGateway;
    private final double complexityThreshold;

    public ModelRouter(LocalModelGateway localModelGateway,
                       @Value("${spintale.ai.routing.complexity-threshold:0.6}") double complexityThreshold) {
        this.localModelGateway = localModelGateway;
        this.complexityThreshold = complexityThreshold;
    }

    public RoutingResult route(String query) {
        double complexity = calculateComplexity(query);
        boolean useLocalModel = localModelGateway.isAvailable() && complexity < complexityThreshold;
        String selectedModel = useLocalModel ? localModelGateway.getModelName() : "default-provider";
        return new RoutingResult(selectedModel, complexity, useLocalModel,
                useLocalModel ? 50 : 800,
                useLocalModel ? 0.001 : 0.02);
    }

    public ChatModel getActualModel(RoutingResult result) {
        return result != null && result.useLocalModel() ? localModelGateway.getChatModel() : null;
    }

    private double calculateComplexity(String query) {
        if (query == null || query.isBlank()) {
            return 0.0;
        }
        double score = Math.min(query.length() / 200.0, 1.0) * 0.3;
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

    public record RoutingResult(
            String selectedModel,
            double complexity,
            boolean useLocalModel,
            long estimatedLatencyMs,
            double estimatedCostUsd) {
    }
}
