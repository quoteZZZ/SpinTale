package com.spintale.ai.retrieval.knowledge;

import java.time.Instant;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class KnowledgeBase
{
    private String kbId;
    private String name;
    private String description;
    private String embeddingModel;
    private int vectorDimension;
    private int chunkSize;
    private int chunkOverlap;
    private String indexType;
    private String metricType;
    private int documentCount;
    private int chunkCount;
    private long totalTokens;
    private KnowledgeBaseStatus status;
    private Map<String, Object> config;
    private Long createBy;
    private Instant createTime;
    private Instant updateTime;

    public enum KnowledgeBaseStatus
    {
        CREATING,
        ACTIVE,
        UPDATING,
        DELETING,
        DELETED,
        ERROR
    }

    public boolean isActive()
    {
        return status == KnowledgeBaseStatus.ACTIVE;
    }

    public boolean canAddDocument()
    {
        return status == KnowledgeBaseStatus.ACTIVE;
    }

    public void incrementDocumentCount()
    {
        this.documentCount++;
    }

    public void decrementDocumentCount()
    {
        if (this.documentCount > 0)
        {
            this.documentCount--;
        }
    }

    public void incrementChunkCount(int count)
    {
        this.chunkCount += count;
    }

    public void addTokens(long tokens)
    {
        this.totalTokens += tokens;
    }

    public KnowledgeBaseStats getStats()
    {
        return new KnowledgeBaseStats(documentCount, chunkCount, totalTokens);
    }

    public static KnowledgeBase create(String name, String embeddingModel)
    {
        return KnowledgeBase.builder()
                .kbId(java.util.UUID.randomUUID().toString())
                .name(name)
                .embeddingModel(embeddingModel)
                .chunkSize(500)
                .chunkOverlap(50)
                .documentCount(0)
                .chunkCount(0)
                .totalTokens(0)
                .status(KnowledgeBaseStatus.CREATING)
                .createTime(Instant.now())
                .updateTime(Instant.now())
                .build();
    }

    public static KnowledgeBase create(String name, String embeddingModel, int chunkSize)
    {
        KnowledgeBase kb = create(name, embeddingModel);
        kb.setChunkSize(chunkSize);
        return kb;
    }

    public record KnowledgeBaseStats(
            int documentCount,
            int chunkCount,
            long totalTokens
    ) {
        public double getAvgChunksPerDocument()
        {
            return documentCount > 0 ? (double) chunkCount / documentCount : 0;
        }

        public double getAvgTokensPerChunk()
        {
            return chunkCount > 0 ? (double) totalTokens / chunkCount : 0;
        }
    }
}
