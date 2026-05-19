package com.spintale.ai.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Token 用量
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenUsage {

    @Builder.Default
    private Integer promptTokens = 0;
    @Builder.Default
    private Integer completionTokens = 0;
    @Builder.Default
    private Integer totalTokens = 0;
}
