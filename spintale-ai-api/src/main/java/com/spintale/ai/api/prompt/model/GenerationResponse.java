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
 * AI 鍐呭鐢熸垚鍝嶅簲
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerationResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 鐢熸垚鐨勫唴瀹?
     */
    private String content;

    /**
     * 鍐呭绫诲瀷
     */
    private ContentType contentType;

    /**
     * 浣跨敤鐨勬ā鍨?
     */
    private String model;

    /**
     * Token 浣跨敤鎯呭喌
     */
    private TokenUsage tokenUsage;

    /**
     * 鐢熸垚鏃堕棿
     */
    @Builder.Default
    private LocalDateTime generatedAt = LocalDateTime.now();

    /**
     * 鏄惁瀹屾垚
     */
    @Builder.Default
    private boolean completed = true;

    /**
     * 閿欒淇℃伅
     */
    private String errorMessage;

    /**
     * 棰濆鍏冩暟鎹?
     */
    private Object metadata;

    /**
     * 瀹屾垚鍘熷洜
     */
    private String finishReason;

    /**
     * 鎺ㄧ悊杞ㄨ抗
     */
    private String reasoningTrace;

    /**
     * 妫?绱㈠埌鐨勪笂涓嬫枃
     */
    private List<String> retrievedContext;

    /**
     * 鏄惁闇?瑕佹墽琛屽伐鍏?
     */
    @Builder.Default
    private boolean requiresToolExecution = false;

    /**
     * 宸ュ叿鍚嶇О
     */
    private String toolName;

    /**
     * 宸ュ叿鍙傛暟
     */
    private Object toolArgs;

    /**
     * 骞昏妫?娴嬪垎鏁?(0.0 - 1.0)
     */
    private Double hallucinationScore;

    /**
     * 杩唬娆℃暟
     */
    private Integer iterations;

    /**
     * 鏋勫缓鎴愬姛鍝嶅簲
     */
    public static GenerationResponse success(String content, ContentType contentType) {
        return GenerationResponse.builder()
                .content(content)
                .contentType(contentType)
                .build();
    }

    /**
     * 鏋勫缓閿欒鍝嶅簲
     */
    public static GenerationResponse error(String errorMessage) {
        return GenerationResponse.builder()
                .completed(false)
                .errorMessage(errorMessage)
                .build();
    }

    /**
     * 妫?鏌ユ槸鍚﹂渶瑕佹墽琛屽伐鍏?
     */
    public boolean requiresToolExecution() {
        return requiresToolExecution;
    }
}
