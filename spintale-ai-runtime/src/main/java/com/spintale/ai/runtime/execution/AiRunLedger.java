package com.spintale.ai.runtime.execution;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface AiRunLedger
{
    String recordRun(AiRunContext context);

    void updateRun(String runId, AiRunResult result);

    void recordSpan(AiRunSpan span);

    List<AiRunSpan> getSpans(String runId);

    Optional<AiRunContext> getContext(String runId);

    Optional<AiRunResult> getResult(String runId);

    List<AiRunResult> queryRuns(String userId, Instant start, Instant end, int limit);

    CostSummary getCostSummary(String userId, Instant start, Instant end);

    record CostSummary(
            long totalRuns,
            long totalInputTokens,
            long totalOutputTokens,
            double totalCost
    ) {}
}
