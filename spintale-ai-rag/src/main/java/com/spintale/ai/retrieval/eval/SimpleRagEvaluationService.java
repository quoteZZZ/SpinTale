package com.spintale.ai.retrieval.eval;

import java.util.*;
import org.springframework.stereotype.Component;
import com.spintale.ai.retrieval.citation.Citation;

@Component
public class SimpleRagEvaluationService implements RagEvaluationService
{
    @Override
    public RagEvaluationResult evaluate(RagEvaluationRequest request)
    {
        RagEvaluationResult.RagEvaluationResultBuilder builder = RagEvaluationResult.builder()
                .evaluationId(UUID.randomUUID().toString())
                .runId(request.runId())
                .query(request.query())
                .answer(request.generatedAnswer())
                .retrievedDocuments(request.retrievedDocumentIds());

        Map<String, Double> metrics = new HashMap<>();

        metrics.put("retrieval_count", (double) request.retrievedDocumentIds().size());

        if (request.expectedDocuments() != null && !request.expectedDocuments().isEmpty())
        {
            double recall = calculateRecall(request.retrievedDocumentIds(), request.expectedDocuments());
            metrics.put("retrieval_recall", recall);

            double precision = calculatePrecision(request.retrievedDocumentIds(), request.expectedDocuments());
            metrics.put("retrieval_precision", precision);
        }

        if (request.citations() != null && !request.citations().isEmpty())
        {
            double citationCoverage = calculateCitationCoverage(request.citations(), request.retrievedDocumentIds());
            metrics.put("citation_coverage", citationCoverage);

            List<RagEvaluationResult.CitationInfo> citationInfos = request.citations().stream()
                    .map(c -> RagEvaluationResult.CitationInfo.builder()
                            .documentId(c.getDocumentId())
                            .chunkId(c.getChunkId() != null ? c.getChunkId().toString() : null)
                            .relevanceScore(c.getRelevanceScore())
                            .usedInAnswer(true)
                            .build())
                    .toList();
            builder.citations(citationInfos);
        }

        if (request.generatedAnswer() != null)
        {
            metrics.put("answer_length", (double) request.generatedAnswer().length());
            metrics.put("answer_word_count", (double) request.generatedAnswer().split("\\s+").length);
        }

        builder.metrics(metrics);
        builder.overallScore(calculateOverallScore(metrics));
        builder.status(RagEvaluationResult.EvaluationStatus.COMPLETED);

        return builder.build();
    }

    @Override
    public List<RagEvaluationResult> evaluateBatch(List<RagEvaluationRequest> requests)
    {
        return requests.stream().map(this::evaluate).toList();
    }

    @Override
    public EvaluationMetrics calculateMetrics(RagEvaluationResult result)
    {
        return new EvaluationMetrics(
                result.getMetric("retrieval_recall"),
                result.getMetric("retrieval_precision"),
                result.getMetric("retrieval_ndcg"),
                result.getMetric("answer_relevance"),
                result.getMetric("faithfulness"),
                result.getMetric("citation_coverage"),
                result.getMetric("groundedness")
        );
    }

    @Override
    public List<EvaluationMetricDefinition> getAvailableMetrics()
    {
        return List.of(
                new EvaluationMetricDefinition("retrieval_recall", "检索召回率", "retrieval", 0, 1, true),
                new EvaluationMetricDefinition("retrieval_precision", "检索精确率", "retrieval", 0, 1, true),
                new EvaluationMetricDefinition("retrieval_ndcg", "归一化折损累计增益", "retrieval", 0, 1, true),
                new EvaluationMetricDefinition("answer_relevance", "答案相关性", "generation", 0, 1, true),
                new EvaluationMetricDefinition("faithfulness", "答案忠实度", "generation", 0, 1, true),
                new EvaluationMetricDefinition("citation_coverage", "引用覆盖率", "citation", 0, 1, true),
                new EvaluationMetricDefinition("groundedness", "答案依据度", "generation", 0, 1, true)
        );
    }

    private double calculateRecall(List<String> retrieved, List<String> expected)
    {
        if (expected == null || expected.isEmpty()) return 1.0;
        long found = retrieved.stream().filter(expected::contains).count();
        return (double) found / expected.size();
    }

    private double calculatePrecision(List<String> retrieved, List<String> expected)
    {
        if (retrieved == null || retrieved.isEmpty()) return 0.0;
        long found = retrieved.stream().filter(expected::contains).count();
        return (double) found / retrieved.size();
    }

    private double calculateCitationCoverage(List<Citation> citations, List<String> retrievedDocs)
    {
        if (retrievedDocs == null || retrievedDocs.isEmpty()) return 0.0;
        long cited = citations.stream()
                .map(Citation::getDocumentId)
                .distinct()
                .count();
        return (double) cited / retrievedDocs.size();
    }

    private double calculateOverallScore(Map<String, Double> metrics)
    {
        double[] weights = {0.25, 0.25, 0.25, 0.25};
        String[] keys = {"retrieval_recall", "retrieval_precision", "answer_relevance", "citation_coverage"};

        double score = 0;
        for (int i = 0; i < keys.length; i++)
        {
            score += weights[i] * metrics.getOrDefault(keys[i], 0.0);
        }
        return score;
    }
}
