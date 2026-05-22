package com.spintale.ai.runtime.execution;

import java.time.Instant;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import com.spintale.ai.core.model.ChatMessage;

@Data
@Builder
public class AiRunResult
{
    private String runId;
    private String traceId;
    private AiRunContext.RunStatus status;
    private String content;
    private List<ChatMessage> messages;
    private Long inputTokens;
    private Long outputTokens;
    private Double cost;
    private Long durationMs;
    private String model;
    private String provider;
    private String errorMessage;
    private String errorCode;
    private Instant startTime;
    private Instant endTime;
    private Object metadata;

    public boolean isSuccess()
    {
        return status == AiRunContext.RunStatus.SUCCEEDED;
    }

    public boolean isFailed()
    {
        return status == AiRunContext.RunStatus.FAILED 
            || status == AiRunContext.RunStatus.TIMEOUT;
    }

    public static AiRunResult success(AiRunContext context, String content)
    {
        return AiRunResult.builder()
                .runId(context.getRunId())
                .traceId(context.getTraceId())
                .status(AiRunContext.RunStatus.SUCCEEDED)
                .content(content)
                .model(context.getModel())
                .provider(context.getProvider())
                .startTime(context.getStartTime())
                .endTime(Instant.now())
                .durationMs(context.getDurationMs())
                .build();
    }

    public static AiRunResult success(AiRunContext context, String content, 
            Long inputTokens, Long outputTokens, Double cost)
    {
        return AiRunResult.builder()
                .runId(context.getRunId())
                .traceId(context.getTraceId())
                .status(AiRunContext.RunStatus.SUCCEEDED)
                .content(content)
                .inputTokens(inputTokens)
                .outputTokens(outputTokens)
                .cost(cost)
                .model(context.getModel())
                .provider(context.getProvider())
                .startTime(context.getStartTime())
                .endTime(Instant.now())
                .durationMs(context.getDurationMs())
                .build();
    }

    public static AiRunResult failure(AiRunContext context, String errorMessage)
    {
        return AiRunResult.builder()
                .runId(context.getRunId())
                .traceId(context.getTraceId())
                .status(AiRunContext.RunStatus.FAILED)
                .errorMessage(errorMessage)
                .model(context.getModel())
                .provider(context.getProvider())
                .startTime(context.getStartTime())
                .endTime(Instant.now())
                .durationMs(context.getDurationMs())
                .build();
    }

    public static AiRunResult failure(AiRunContext context, 
            String errorCode, String errorMessage)
    {
        AiRunResult result = failure(context, errorMessage);
        result.setErrorCode(errorCode);
        return result;
    }

    public static AiRunResult timeout(AiRunContext context)
    {
        return AiRunResult.builder()
                .runId(context.getRunId())
                .traceId(context.getTraceId())
                .status(AiRunContext.RunStatus.TIMEOUT)
                .errorMessage("Execution timed out after " + context.getTimeoutMs() + "ms")
                .errorCode("TIMEOUT")
                .model(context.getModel())
                .provider(context.getProvider())
                .startTime(context.getStartTime())
                .endTime(Instant.now())
                .durationMs(context.getDurationMs())
                .build();
    }
}
