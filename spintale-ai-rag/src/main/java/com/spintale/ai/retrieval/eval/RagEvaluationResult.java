package com.spintale.ai.retrieval.eval;

import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RagEvaluationResult
{
    private String evaluationId;
    private String runId;
    private String query;
    private String answer;
    private List<String> retrievedDocuments;
    private List<CitationInfo> citations;
    private Map<String, Double> metrics;
    private double overallScore;
    private String feedback;
    private EvaluationStatus status;

    public enum EvaluationStatus
    {
        PENDING,
        COMPLETED,
        FAILED
    }

    @Data
    @Builder
    public static class CitationInfo
    {
        private String documentId;
        private String chunkId;
        private double relevanceScore;
        private boolean usedInAnswer;
    }

    public double getMetric(String name)
    {
        return metrics != null ? metrics.getOrDefault(name, 0.0) : 0.0;
    }

    public void setMetric(String name, double value)
    {
        if (this.metrics == null)
        {
            this.metrics = new java.util.HashMap<>();
        }
        this.metrics.put(name, value);
    }

    public boolean hasMetric(String name)
    {
        return metrics != null && metrics.containsKey(name);
    }
}
