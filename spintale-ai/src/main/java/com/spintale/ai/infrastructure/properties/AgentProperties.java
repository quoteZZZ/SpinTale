package com.spintale.ai.infrastructure.properties;

import lombok.Data;

/**
 * Agent runtime settings.
 */
@Data
public class AgentProperties {
    private ReactConfig react = new ReactConfig();
    private CoordinationConfig coordination = new CoordinationConfig();

    @Data
    public static class ReactConfig {
        private Boolean enabled = true;
        private Integer maxIterations = 10;
        private Long toolTimeoutMs = 30000L;
        private Integer loopThreshold = 3;
    }

    @Data
    public static class CoordinationConfig {
        private Boolean enabled = false;
    }
}
