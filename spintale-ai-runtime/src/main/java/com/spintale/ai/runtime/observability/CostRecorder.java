package com.spintale.ai.runtime.observability;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class CostRecorder
{
    private final List<CostRecord> records = new CopyOnWriteArrayList<>();
    private final Map<String, List<CostRecord>> recordsByUser = new ConcurrentHashMap<>();
    private final Map<String, List<CostRecord>> recordsByModel = new ConcurrentHashMap<>();

    public void record(CostRecord record)
    {
        records.add(record);

        if (record.getUserId() != null)
        {
            recordsByUser.computeIfAbsent(record.getUserId(), k -> new CopyOnWriteArrayList<>())
                    .add(record);
        }

        if (record.getModel() != null)
        {
            recordsByModel.computeIfAbsent(record.getModel(), k -> new CopyOnWriteArrayList<>())
                    .add(record);
        }
    }

    public CostRecord record(String runId, String model, String provider,
            long inputTokens, long outputTokens, double cost)
    {
        CostRecord record = CostRecord.chat(runId, model, provider, inputTokens, outputTokens, cost);
        record(record);
        return record;
    }

    public List<CostRecord> getRecordsByUser(String userId)
    {
        return recordsByUser.getOrDefault(userId, List.of());
    }

    public List<CostRecord> getRecordsByModel(String model)
    {
        return recordsByModel.getOrDefault(model, List.of());
    }

    public List<CostRecord> getRecordsByTimeRange(Instant start, Instant end)
    {
        return records.stream()
                .filter(r -> r.getTimestamp() != null)
                .filter(r -> !r.getTimestamp().isBefore(start) && !r.getTimestamp().isAfter(end))
                .collect(Collectors.toList());
    }

    public CostSummary getSummary(String userId, Instant start, Instant end)
    {
        List<CostRecord> filtered = records.stream()
                .filter(r -> userId == null || userId.equals(r.getUserId()))
                .filter(r -> start == null || (r.getTimestamp() != null && !r.getTimestamp().isBefore(start)))
                .filter(r -> end == null || (r.getTimestamp() != null && !r.getTimestamp().isAfter(end)))
                .collect(Collectors.toList());

        long totalInputTokens = filtered.stream().mapToLong(CostRecord::getInputTokens).sum();
        long totalOutputTokens = filtered.stream().mapToLong(CostRecord::getOutputTokens).sum();
        long totalTokens = filtered.stream().mapToLong(CostRecord::getTotalTokens).sum();
        double totalCost = filtered.stream().mapToDouble(CostRecord::getCost).sum();

        return new CostSummary(
                filtered.size(),
                totalInputTokens,
                totalOutputTokens,
                totalTokens,
                totalCost,
                "USD"
        );
    }

    public CostSummary getSummaryToday(String userId)
    {
        Instant startOfDay = Instant.now().truncatedTo(ChronoUnit.DAYS);
        return getSummary(userId, startOfDay, null);
    }

    public CostSummary getSummaryThisMonth(String userId)
    {
        Instant startOfMonth = Instant.now().truncatedTo(ChronoUnit.DAYS).minus(30, ChronoUnit.DAYS);
        return getSummary(userId, startOfMonth, null);
    }

    public ModelCostBreakdown getModelBreakdown(Instant start, Instant end)
    {
        Map<String, Double> costByModel = records.stream()
                .filter(r -> start == null || (r.getTimestamp() != null && !r.getTimestamp().isBefore(start)))
                .filter(r -> end == null || (r.getTimestamp() != null && !r.getTimestamp().isAfter(end)))
                .collect(Collectors.groupingBy(
                        CostRecord::getModel,
                        Collectors.summingDouble(CostRecord::getCost)
                ));

        return new ModelCostBreakdown(costByModel);
    }

    public void clear()
    {
        records.clear();
        recordsByUser.clear();
        recordsByModel.clear();
    }

    public record CostSummary(
            long recordCount,
            long totalInputTokens,
            long totalOutputTokens,
            long totalTokens,
            double totalCost,
            String currency
    ) {}

    public record ModelCostBreakdown(Map<String, Double> costByModel)
    {
        public double getTotalCost()
        {
            return costByModel.values().stream().mapToDouble(Double::doubleValue).sum();
        }

        public List<Map.Entry<String, Double>> getTopModels(int limit)
        {
            return costByModel.entrySet().stream()
                    .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                    .limit(limit)
                    .collect(Collectors.toList());
        }
    }
}
