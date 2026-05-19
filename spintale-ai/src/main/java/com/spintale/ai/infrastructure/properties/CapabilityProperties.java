package com.spintale.ai.infrastructure.properties;

import lombok.Data;

/**
 * Cross-cutting AI capability settings.
 */
public final class CapabilityProperties {

    private CapabilityProperties() {
    }

    @Data
    public static class ContextConfig {
        private Integer maxMessages = 20;
        private Double memoryRetrievalThreshold = 0.6;
        private Integer maxRetrievedMemories = 5;
        private Boolean longTermMemoryEnabled = true;
    }

    @Data
    public static class HallucinationDetectionConfig {
        private Boolean enabled = true;
        private Double threshold = 0.5;
        private String action = "WARN";
    }

    @Data
    public static class SafetyConfig {
        private Boolean enabled = true;
        private String level = "MODERATE";
    }

    @Data
    public static class SemanticCacheConfig {
        private Boolean enabled = false;
        private Double similarityThreshold = 0.85;
        private Long ttlHours = 24L;
    }

    @Data
    public static class ExperimentConfig {
        private Boolean enabled = false;
    }
}
