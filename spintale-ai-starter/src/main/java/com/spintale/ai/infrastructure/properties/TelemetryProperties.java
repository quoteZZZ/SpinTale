package com.spintale.ai.infrastructure.properties;

import lombok.Data;

/**
 * Telemetry and observability settings.
 */
@Data
public class TelemetryProperties {
    
    private Boolean enabled = true;
    
    private String serviceName = "spintale-ai";
    
    private OtlpConfig otlp = new OtlpConfig();
    
    @Data
    public static class OtlpConfig {
        private String endpoint = "http://localhost:4317";
        private Boolean enabled = true;
    }
}
