package com.spintale.ai.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * RAG 检索结果 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
}
