package com.spintale.ai.retrieval.citation;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Citation
{
    private String citationId;
    private String documentId;
    private String documentName;
    private Long chunkId;
    private Integer chunkIndex;
    private String content;
    private Integer pageNumber;
    private String sectionPath;
    private double relevanceScore;
    private String snippet;
    private SourceLocation sourceLocation;

    @Data
    @Builder
    public static class SourceLocation
    {
        private Integer startChar;
        private Integer endChar;
        private Integer startLine;
        private Integer endLine;
    }

    public String getDisplayReference()
    {
        StringBuilder ref = new StringBuilder();
        ref.append("[").append(documentName != null ? documentName : documentId).append("]");
        if (pageNumber != null)
        {
            ref.append(" 第").append(pageNumber).append("页");
        }
        if (chunkIndex != null)
        {
            ref.append(" 片段").append(chunkIndex);
        }
        return ref.toString();
    }

    public static Citation of(String docId, String docName, String content)
    {
        return Citation.builder()
                .citationId(java.util.UUID.randomUUID().toString())
                .documentId(docId)
                .documentName(docName)
                .content(content)
                .build();
    }

    public static Citation of(String docId, String docName, 
            String content, int pageNumber, int chunkIndex)
    {
        return Citation.builder()
                .citationId(java.util.UUID.randomUUID().toString())
                .documentId(docId)
                .documentName(docName)
                .content(content)
                .pageNumber(pageNumber)
                .chunkIndex(chunkIndex)
                .build();
    }
}
