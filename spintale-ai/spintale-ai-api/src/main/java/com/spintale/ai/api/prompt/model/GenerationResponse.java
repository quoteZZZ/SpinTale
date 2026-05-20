package com.spintale.ai.generation.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import com.spintale.ai.core.model.TokenUsage;
import com.spintale.ai.generation.model.GenerationRequest.ContentType;

/**
 * AI 内容生成响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerationResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 生成的内�?
     */
    private String content;

    /**
     * 内容类型
     */
    private ContentType contentType;

    /**
     * 使用的模�?
     */
    private String model;

    /**
     * Token 使用情况
     */
    private TokenUsage tokenUsage;

    /**
     * 生成时间
     */
    @Builder.Default
    private LocalDateTime generatedAt = LocalDateTime.now();

    /**
     * 是否完成
     */
    @Builder.Default
    private boolean completed = true;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 额外元数�?
     */
    private Object metadata;

    /**
     * 完成原因
     */
    private String finishReason;

    /**
     * 推理轨迹
     */
    private String reasoningTrace;

    /**
     * 检索到的上下文
     */
    private List<String> retrievedContext;

    /**
     * 是否需要执行工�?
     */
    @Builder.Default
    private boolean requiresToolExecution = false;

    /**
     * 工具名称
     */
    private String toolName;

    /**
     * 工具参数
     */
    private Object toolArgs;

    /**
     * 幻觉检测分�?(0.0 - 1.0)
     */
    private Double hallucinationScore;

    /**
     * 迭代次数
     */
    private Integer iterations;

    /**
     * 构建成功响应
     */
    public static GenerationResponse success(String content, ContentType contentType) {
        return GenerationResponse.builder()
                .content(content)
                .contentType(contentType)
                .build();
    }

    /**
     * 构建错误响应
     */
    public static GenerationResponse error(String errorMessage) {
        return GenerationResponse.builder()
                .completed(false)
                .errorMessage(errorMessage)
                .build();
    }

    /**
     * 检查是否需要执行工�?
     */
    public boolean requiresToolExecution() {
        return requiresToolExecution;
    }
}
