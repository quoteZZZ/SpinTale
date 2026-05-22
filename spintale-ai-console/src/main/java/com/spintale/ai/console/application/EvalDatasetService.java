package com.spintale.ai.console.application;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface EvalDatasetService
{
    EvalDataset createDataset(String name, String description);

    Optional<EvalDataset> getDataset(String datasetId);

    List<EvalDataset> listDatasets();

    void deleteDataset(String datasetId);

    EvalCase addCase(String datasetId, String name, String input, 
            String expectedOutput);

    EvalCase addCase(String datasetId, String name, String input, 
            String expectedOutput, java.util.Map<String, Object> metadata);

    List<EvalCase> listCases(String datasetId);

    void removeCase(String caseId);

    EvalResult runEvaluation(String datasetId, String model);

    List<EvalResult> getResults(String datasetId);

    Optional<EvalResult> getResult(String resultId);

    record EvalDataset(
            String datasetId,
            String name,
            String description,
            int caseCount,
            String status,
            Instant createTime
    ) {}

    record EvalCase(
            String caseId,
            String datasetId,
            String name,
            String input,
            String expectedOutput,
            java.util.Map<String, Object> metadata,
            Instant createTime
    ) {}

    record EvalResult(
            String resultId,
            String datasetId,
            String model,
            int totalCases,
            int successCount,
            double overallScore,
            java.util.Map<String, Double> metrics,
            Instant createTime
    ) {}
}
