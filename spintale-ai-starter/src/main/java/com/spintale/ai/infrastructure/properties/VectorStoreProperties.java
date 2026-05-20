package com.spintale.ai.infrastructure.properties;

import lombok.Data;

/**
 * Vector store settings for RAG.
 */
@Data
public class VectorStoreProperties {
    
    private MilvusConfig milvus = new MilvusConfig();
    
    @Data
    public static class MilvusConfig {
        private Boolean enabled = false;
        
        private String host = "localhost";
        
        private Integer port = 19530;
        
        private String collectionName = "spintale_vectors";
        
        private Integer dimension = 1536;
        
        private String metricType = "COSINE";
    }
}
