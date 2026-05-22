package com.spintale.ai.retrieval.document;

import java.time.Instant;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Document
{
    private String documentId;
    private String knowledgeBaseId;
    private String name;
    private String source;
    private String sourceType;
    private String mimeType;
    private Long fileSize;
    private DocumentStatus status;
    private String version;
    private String checksum;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant processedAt;
    private Map<String, Object> metadata;
    private String errorMessage;
    private Integer chunkCount;
    private Long totalTokens;

    public enum DocumentStatus
    {
        UPLOADED("已上传"),
        PENDING("待处理"),
        PARSING("解析中"),
        PARSED("已解析"),
        CHUNKING("分块中"),
        CHUNKED("已分块"),
        EMBEDDING("向量化中"),
        INDEXING("索引中"),
        INDEXED("已索引"),
        FAILED("失败"),
        DELETING("删除中"),
        DELETED("已删除");

        private final String description;

        DocumentStatus(String description)
        {
            this.description = description;
        }

        public String getDescription()
        {
            return description;
        }

        public boolean canTransitionTo(DocumentStatus target)
        {
            return switch (this)
            {
                case UPLOADED -> target == PENDING || target == DELETING;
                case PENDING -> target == PARSING || target == FAILED || target == DELETING;
                case PARSING -> target == PARSED || target == FAILED || target == DELETING;
                case PARSED -> target == CHUNKING || target == FAILED || target == DELETING;
                case CHUNKING -> target == CHUNKED || target == FAILED || target == DELETING;
                case CHUNKED -> target == EMBEDDING || target == FAILED || target == DELETING;
                case EMBEDDING -> target == INDEXING || target == FAILED || target == DELETING;
                case INDEXING -> target == INDEXED || target == FAILED || target == DELETING;
                case INDEXED -> target == DELETING;
                case FAILED -> target == PENDING || target == DELETING;
                case DELETING -> target == DELETED || target == FAILED;
                case DELETED -> false;
            };
        }

        public boolean isProcessing()
        {
            return this == PARSING || this == CHUNKING || this == EMBEDDING || this == INDEXING;
        }

        public boolean isTerminal()
        {
            return this == INDEXED || this == FAILED || this == DELETED;
        }

        public boolean canRetry()
        {
            return this == FAILED;
        }
    }

    public boolean transitionTo(DocumentStatus newStatus)
    {
        if (status.canTransitionTo(newStatus))
        {
            this.status = newStatus;
            this.updatedAt = Instant.now();
            return true;
        }
        return false;
    }

    public void markAsParsing()
    {
        transitionTo(DocumentStatus.PARSING);
    }

    public void markAsParsed()
    {
        transitionTo(DocumentStatus.PARSED);
    }

    public void markAsChunking()
    {
        transitionTo(DocumentStatus.CHUNKING);
    }

    public void markAsChunked(int chunkCount)
    {
        if (transitionTo(DocumentStatus.CHUNKED))
        {
            this.chunkCount = chunkCount;
        }
    }

    public void markAsEmbedding()
    {
        transitionTo(DocumentStatus.EMBEDDING);
    }

    public void markAsIndexing()
    {
        transitionTo(DocumentStatus.INDEXING);
    }

    public void markAsIndexed()
    {
        if (transitionTo(DocumentStatus.INDEXED))
        {
            this.processedAt = Instant.now();
        }
    }

    public void markAsFailed(String error)
    {
        if (transitionTo(DocumentStatus.FAILED))
        {
            this.errorMessage = error;
        }
    }

    public void retry()
    {
        if (status.canRetry())
        {
            this.status = DocumentStatus.PENDING;
            this.errorMessage = null;
            this.updatedAt = Instant.now();
        }
    }

    public static Document create(String kbId, String name, String source, String mimeType)
    {
        return Document.builder()
                .documentId(java.util.UUID.randomUUID().toString())
                .knowledgeBaseId(kbId)
                .name(name)
                .source(source)
                .mimeType(mimeType)
                .status(DocumentStatus.UPLOADED)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
}
