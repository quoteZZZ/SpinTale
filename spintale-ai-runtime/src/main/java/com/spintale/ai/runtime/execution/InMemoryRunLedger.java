package com.spintale.ai.runtime.execution;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class InMemoryRunLedger implements AiRunLedger
{
    private final Map<String, AiRunContext> contexts = new ConcurrentHashMap<>();
    private final Map<String, AiRunResult> results = new ConcurrentHashMap<>();
    private final Map<String, List<AiRunSpan>> spans = new ConcurrentHashMap<>();

    @Override
    public String recordRun(AiRunContext context)
    {
        contexts.put(context.getRunId(), context);
        spans.put(context.getRunId(), new ArrayList<>());
        return context.getRunId();
    }

    @Override
    public void updateRun(String runId, AiRunResult result)
    {
        results.put(runId, result);
    }

    @Override
    public void recordSpan(AiRunSpan span)
    {
        List<AiRunSpan> spanList = spans.computeIfAbsent(
                span.getRunId(), k -> new ArrayList<>());
        spanList.add(span);
    }

    @Override
    public List<AiRunSpan> getSpans(String runId)
    {
        return spans.getOrDefault(runId, Collections.emptyList());
    }

    @Override
    public Optional<AiRunContext> getContext(String runId)
    {
        return Optional.ofNullable(contexts.get(runId));
    }

    @Override
    public Optional<AiRunResult> getResult(String runId)
    {
        return Optional.ofNullable(results.get(runId));
    }

    @Override
    public List<AiRunResult> queryRuns(String userId, Instant start, 
            Instant end, int limit)
    {
        return results.values().stream()
                .filter(r -> {
                    if (userId != null)
                    {
                        AiRunContext ctx = contexts.get(r.getRunId());
                        if (ctx == null || !userId.equals(ctx.getUserId()))
                        {
                            return false;
                        }
                    }
                    if (start != null && r.getStartTime() != null 
                            && r.getStartTime().isBefore(start))
                    {
                        return false;
                    }
                    if (end != null && r.getEndTime() != null 
                            && r.getEndTime().isAfter(end))
                    {
                        return false;
                    }
                    return true;
                })
                .sorted(Comparator.comparing(AiRunResult::getStartTime).reversed())
                .limit(limit)
                .toList();
    }

    @Override
    public CostSummary getCostSummary(String userId, Instant start, Instant end)
    {
        List<AiRunResult> userRuns = queryRuns(userId, start, end, Integer.MAX_VALUE);
        
        long totalRuns = userRuns.size();
        long totalInputTokens = userRuns.stream()
                .mapToLong(r -> r.getInputTokens() != null ? r.getInputTokens() : 0)
                .sum();
        long totalOutputTokens = userRuns.stream()
                .mapToLong(r -> r.getOutputTokens() != null ? r.getOutputTokens() : 0)
                .sum();
        double totalCost = userRuns.stream()
                .mapToDouble(r -> r.getCost() != null ? r.getCost() : 0.0)
                .sum();
        
        return new CostSummary(totalRuns, totalInputTokens, totalOutputTokens, totalCost);
    }

    public void clear()
    {
        contexts.clear();
        results.clear();
        spans.clear();
    }

    public int getRunCount()
    {
        return contexts.size();
    }
}
