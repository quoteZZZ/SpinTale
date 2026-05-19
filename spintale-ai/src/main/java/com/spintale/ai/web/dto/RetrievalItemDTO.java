package com.spintale.ai.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 检索项 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RetrievalItemDTO implements Serializable {
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
}
