package com.spintale.ai.infrastructure.properties;

import lombok.Data;

/**
 * Retrieval augmented generation settings.
 */
@Data
public class RagProperties {
    private Boolean enabled = false;
    private String vectorStore = "milvus";
    private String embeddingModel = "bge-small-en-v1.5";
    private Integer maxRetrievedDocs = 5;
    private Double minScore = 0.5;
    private Boolean queryRewritingEnabled = false;
    private Integer maxSegmentSize = 800;
    private Integer maxOverlapSize = 120;
    private GraphConfig graph = new GraphConfig();
    private MilvusConfig milvus = new MilvusConfig();

    @Data
    public static class GraphConfig {
        private Boolean enabled = false;
    }

    @Data
    public static class MilvusConfig {
        private String uri = "http://localhost:19530";
        private String username;
        private String password;
        private String collectionName = "spintale_knowledge";
    }
}
