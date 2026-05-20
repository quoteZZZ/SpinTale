package com.spintale.ai.infrastructure.properties;

import lombok.Data;

/**
 * Memory management and compression settings.
 */
@Data
public class MemoryProperties {
    
    private SessionConfig session = new SessionConfig();
    
    private CompressionConfig compression = new CompressionConfig();
    
    @Data
    public static class SessionConfig {
        private Integer expireMinutes = 30;
        
        private Integer maxSize = 10000;
    }
    
    @Data
    public static class CompressionConfig {
        private Boolean enabled = true;
        
        private Integer maxTokens = 4000;
        
        private Integer summaryThreshold = 6;
    }
}
