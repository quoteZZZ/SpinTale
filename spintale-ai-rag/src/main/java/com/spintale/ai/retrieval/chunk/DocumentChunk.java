package com.spintale.ai.retrieval.chunk;

import java.time.Instant;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DocumentChunk
{
    private String chunkId;
    private String documentId;
    private String knowledgeBaseId;
    private Integer chunkIndex;
    private String content;
    private Integer tokenCount;
    private Integer charStart;
    private Integer charEnd;
    private Integer pageNumber;
    private String sectionPath;
    private Map<String, Object> metadata;
    private String checksum;
    private float[] embedding;
    private Instant createdAt;

    public boolean hasEmbedding()
    {
        return embedding != null && embedding.length > 0;
    }

    public int getEmbeddingDimension()
    {
        return embedding != null ? embedding.length : 0;
    }

    public ChunkLocation getLocation()
    {
        return new ChunkLocation(documentId, chunkIndex, pageNumber, sectionPath);
    }

    public record ChunkLocation(
            String documentId,
            Integer chunkIndex,
            Integer pageNumber,
            String sectionPath
    ) {
        public String toDisplayString()
        {
            StringBuilder sb = new StringBuilder();
            sb.append("Doc: ").append(documentId);
            if (pageNumber != null)
            {
                sb.append(", Page: ").append(pageNumber);
            }
            if (chunkIndex != null)
            {
                sb.append(", Chunk: ").append(chunkIndex);
            }
            return sb.toString();
        }
    }

    public static DocumentChunk of(String documentId, String kbId, 
            int index, String content)
    {
        return DocumentChunk.builder()
                .chunkId(java.util.UUID.randomUUID().toString())
                .documentId(documentId)
                .knowledgeBaseId(kbId)
                .chunkIndex(index)
                .content(content)
                .tokenCount(estimateTokens(content))
                .createdAt(Instant.now())
                .build();
    }

    public static DocumentChunk of(String documentId, String kbId, 
            int index, String content, int pageNumber)
    {
        return DocumentChunk.builder()
                .chunkId(java.util.UUID.randomUUID().toString())
                .documentId(documentId)
                .knowledgeBaseId(kbId)
                .chunkIndex(index)
                .content(content)
                .pageNumber(pageNumber)
                .tokenCount(estimateTokens(content))
                .createdAt(Instant.now())
                .build();
    }

    private static int estimateTokens(String text)
    {
        if (text == null) return 0;
        return (int) Math.ceil(text.length() / 4.0);
    }
}
