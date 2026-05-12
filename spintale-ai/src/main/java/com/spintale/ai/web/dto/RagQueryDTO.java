package com.spintale.ai.web.dto;

import java.io.Serializable;
import java.util.List;

/**
 * RAG 知识库检索请求 DTO
 */
public class RagQueryDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 查询文本
     */
    private String query;

    /**
     * 最大返回结果数（默认 5）
     */
    private Integer maxResults = 5;

    /**
     * 最小相似度阈值（0.0-1.0，默认 0.0）
     */
    private Double minScore = 0.0;

    /**
     * 知识库 ID 列表（可选，指定检索的知识库）
     */
    private List<String> knowledgeBaseIds;

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public Integer getMaxResults() {
        return maxResults;
    }

    public void setMaxResults(Integer maxResults) {
        this.maxResults = maxResults;
    }

    public Double getMinScore() {
        return minScore;
    }

    public void setMinScore(Double minScore) {
        this.minScore = minScore;
    }

    public List<String> getKnowledgeBaseIds() {
        return knowledgeBaseIds;
    }

    public void setKnowledgeBaseIds(List<String> knowledgeBaseIds) {
        this.knowledgeBaseIds = knowledgeBaseIds;
    }
}
