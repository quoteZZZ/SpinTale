package com.spintale.ai.runtime.observability;

import java.time.Instant;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CostRecord
{
    private String recordId;
    private String runId;
    private String traceId;
    private String userId;
    private String model;
    private String provider;
    private long inputTokens;
    private long outputTokens;
    private long totalTokens;
    private double cost;
    private String currency;
    private CostType costType;
    private Instant timestamp;

    public enum CostType
    {
        CHAT,
        EMBEDDING,
        RERANK,
        IMAGE,
        AUDIO
    }

    public static CostRecord of(String runId, String model, String provider,
            long inputTokens, long outputTokens, double cost)
    {
        return CostRecord.builder()
                .recordId(java.util.UUID.randomUUID().toString())
                .runId(runId)
                .model(model)
                .provider(provider)
                .inputTokens(inputTokens)
                .outputTokens(outputTokens)
                .totalTokens(inputTokens + outputTokens)
                .cost(cost)
                .currency("USD")
                .timestamp(Instant.now())
                .build();
    }

    public static CostRecord chat(String runId, String model, String provider,
            long inputTokens, long outputTokens, double cost)
    {
        CostRecord record = of(runId, model, provider, inputTokens, outputTokens, cost);
        record.setCostType(CostType.CHAT);
        return record;
    }

    public static CostRecord embedding(String runId, String model, String provider,
            long tokens, double cost)
    {
        CostRecord record = of(runId, model, provider, tokens, 0, cost);
        record.setCostType(CostType.EMBEDDING);
        return record;
    }
}
