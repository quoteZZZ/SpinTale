package com.spintale.ai.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * RAG 知识库检索请求 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RagQueryDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 查询文本
     */
    private String query;

    /**
     * 最大返回结果数（默认 5）
     */
    @Builder.Default
    private Integer maxResults = 5;

    /**
     * 最小相似度阈值（0.0-1.0，默认 0.0）
     */
    @Builder.Default
    private Double minScore = 0.0;

    /**
     * 知识库 ID 列表（可选，指定检索的知识库）
     */
    private List<String> knowledgeBaseIds;
}
