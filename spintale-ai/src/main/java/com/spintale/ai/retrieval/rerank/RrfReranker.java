package com.spintale.ai.retriever.rerank;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 倒数排名融合 (Reciprocal Rank Fusion, RRF) 重排序器
 * 结合多种检索结果（如关键词检索 + 向量检索）进行融合排序
 */
@Slf4j
@Service
public class RrfReranker {

    private static final int DEFAULT_K = 60; // RRF 常数，用于调节排名影响

    /**
     * 执行 RRF 重排序
     * @param retrievalResults 多个检索结果的列表，每个结果是一个文档列表
     * @param topK 返回前 K 个结果
     * @return 重排序后的文档列表
     */
    public List<RetrievalResult> rerank(List<List<RetrievalResult>> retrievalResults, int topK) {
        if (retrievalResults == null || retrievalResults.isEmpty()) {
            return new ArrayList<>();
        }

        // 计算每个文档的 RRF 分数
        Map<String, Double> rrfScores = calculateRrfScores(retrievalResults);

        // 按 RRF 分数降序排序
        List<RetrievalResult> rankedResults = rrfScores.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(topK)
                .map(entry -> {
                    // 找到原始结果中的完整信息
                    for (List<RetrievalResult> resultList : retrievalResults) {
                        for (RetrievalResult result : resultList) {
                            if (result.docId().equals(entry.getKey())) {
                                return new RetrievalResult(
                                        result.docId(),
                                        result.content(),
                                        result.score(),
                                        entry.getValue() // 使用 RRF 分数作为最终分数
                                );
                            }
                        }
                    }
                    return null;
                })
                .filter(result -> result != null)
                .collect(Collectors.toList());

        log.info("RRF 重排序完成：输入 {} 个列表，输出 {} 个结果", retrievalResults.size(), rankedResults.size());
        return rankedResults;
    }

    /**
     * 计算所有文档的 RRF 分数
     */
    private Map<String, Double> calculateRrfScores(List<List<RetrievalResult>> retrievalResults) {
        Map<String, Double> scores = new java.util.HashMap<>();

        for (List<RetrievalResult> results : retrievalResults) {
            for (int rank = 0; rank < results.size(); rank++) {
                RetrievalResult result = results.get(rank);
                double rrfScore = 1.0 / (DEFAULT_K + rank + 1);
                
                scores.merge(result.docId(), rrfScore, Double::sum);
            }
        }

        return scores;
    }

    /**
     * 检索结果记录
     */
    public record RetrievalResult(
            String docId,
            String content,
            double originalScore,
            double finalScore
    ) {}
}
