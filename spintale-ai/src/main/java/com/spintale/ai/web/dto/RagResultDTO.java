package com.spintale.ai.web.dto;

import java.io.Serializable;
import java.util.List;

/**
 * RAG 检索结果 DTO
 */
public class RagResultDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 检索结果列表
     */
    private List<RetrievalItemDTO> results;

    /**
     * 查询耗时（毫秒）
     */
    private Long queryTimeMs;

    public List<RetrievalItemDTO> getResults() {
        return results;
    }

    public void setResults(List<RetrievalItemDTO> results) {
        this.results = results;
    }

    public Long getQueryTimeMs() {
        return queryTimeMs;
    }

    public void setQueryTimeMs(Long queryTimeMs) {
        this.queryTimeMs = queryTimeMs;
    }
}

/**
 * 检索项 DTO
 */
class RetrievalItemDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 内容片段
     */
    private String content;

    /**
     * 相似度分数
     */
    private Double score;

    /**
     * 来源文档 ID
     */
    private String documentId;

    /**
     * 来源文档名称
     */
    private String documentName;

    /**
     * 在文档中的位置（页码/段落号）
     */
    private Integer position;

    /**
     * 元数据
     */
    private Object metadata;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getDocumentName() {
        return documentName;
    }

    public void setDocumentName(String documentName) {
        this.documentName = documentName;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public Object getMetadata() {
        return metadata;
    }

    public void setMetadata(Object metadata) {
        this.metadata = metadata;
    }
}
