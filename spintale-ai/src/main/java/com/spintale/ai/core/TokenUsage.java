package com.spintale.ai.core;

import lombok.Builder;
import lombok.Data;

/**
 * Token 使用统计
 */
@Data
@Builder
public class TokenUsage {
    
    /**
     * 提示词 token 数
     */
    @Builder.Default
    private Integer promptTokens = 0;
    
    /**
     * 完成 token 数
     */
    @Builder.Default
    private Integer completionTokens = 0;
    
    /**
     * 总 token 数
     */
    @Builder.Default
    private Integer totalTokens = 0;
}
