package com.spintale.ai.retrieval.eval;

import java.util.List;
import com.spintale.ai.retrieval.citation.Citation;

public interface RagEvaluationService
{
    RagEvaluationResult evaluate(RagEvaluationRequest request);

    List<RagEvaluationResult> evaluateBatch(List<RagEvaluationRequest> requests);

    EvaluationMetrics calculateMetrics(RagEvaluationResult result);

    List<EvaluationMetricDefinition> getAvailableMetrics();

    record RagEvaluationRequest(
            String runId,
            String query,
            String generatedAnswer,
            List<String> retrievedDocumentIds,
            List<Citation> citations,
            String expectedAnswer,
            List<String> expectedDocuments
    ) {}

    record EvaluationMetrics(
            double retrievalRecall,
            double retrievalPrecision,
            double retrievalNdcg,
            double answerRelevance,
            double faithfulness,
            double citationCoverage,
            double groundedness
    ) {
        public double getOverallScore()
        {
            return (retrievalRecall + answerRelevance + faithfulness + citationCoverage) / 4.0;
        }
    }

    record EvaluationMetricDefinition(
            String name,
            String description,
            String type,
            double minValue,
            double maxValue,
            boolean higherIsBetter
    ) {}
}
