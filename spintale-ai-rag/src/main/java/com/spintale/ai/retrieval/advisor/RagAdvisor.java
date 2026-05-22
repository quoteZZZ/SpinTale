package com.spintale.ai.retrieval.advisor;

import java.util.List;
import java.util.stream.Collectors;

import com.spintale.ai.api.advisor.Advisor;
import com.spintale.ai.api.advisor.AdvisorContext;
import com.spintale.ai.api.advisor.AdvisorOrder;
import com.spintale.ai.api.advisor.AdvisorRequest;
import com.spintale.ai.api.advisor.AdvisorResponse;
import com.spintale.ai.retrieval.vector.RetrievalService;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;

public class RagAdvisor implements Advisor {

    private final RetrievalService retrievalService;
    private int maxRetrievedDocs = 5;
    private double minScore = 0.5;
    private boolean queryRewritingEnabled = false;

    public RagAdvisor(RetrievalService retrievalService) {
        this.retrievalService = retrievalService;
    }

    @Override
    public String getName() {
        return "RagAdvisor";
    }

    @Override
    public int getOrder() {
        return AdvisorOrder.RAG;
    }

    @Override
    public AdvisorRequest adviseRequest(AdvisorRequest request, AdvisorContext context) {
        if (request == null || retrievalService == null || request.getUserMessage() == null) {
            return request;
        }

        String query = queryRewritingEnabled ? request.getUserMessage().trim() : request.getUserMessage();
        List<EmbeddingMatch<TextSegment>> results = retrievalService.search(query, maxRetrievedDocs, minScore);
        context.put(AdvisorContext.RETRIEVED_DOCUMENTS, results);
        if (!results.isEmpty()) {
            String ragContext = results.stream()
                    .map(match -> match.embedded() == null ? "" : match.embedded().text())
                    .collect(Collectors.joining("\n\n---\n\n"));
            String prompt = request.getSystemPrompt() == null ? "" : request.getSystemPrompt();
            request.setSystemPrompt(prompt + "\n\nReference material:\n" + ragContext);
        }
        return request;
    }

    @Override
    public AdvisorResponse adviseResponse(AdvisorResponse response, AdvisorContext context) {
        Object documents = context.getAttribute(AdvisorContext.RETRIEVED_DOCUMENTS);
        if (response != null && documents instanceof List<?> list) {
            response.setMetadata("rag_sources_count", list.size());
        }
        return response;
    }

    public RagAdvisor setMaxRetrievedDocs(int max) {
        this.maxRetrievedDocs = Math.max(1, max);
        return this;
    }

    public RagAdvisor setMinScore(double minScore) {
        this.minScore = Math.max(0.0, Math.min(1.0, minScore));
        return this;
    }

    public RagAdvisor setQueryRewritingEnabled(boolean enabled) {
        this.queryRewritingEnabled = enabled;
        return this;
    }
}
