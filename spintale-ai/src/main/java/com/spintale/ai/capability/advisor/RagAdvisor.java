package com.spintale.ai.capability.advisor;

import com.spintale.ai.retrieval.vector.RetrievalService;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Injects retrieved context before the model call and exposes source metadata
 * after the response.
 */
public class RagAdvisor implements Advisor {

    private static final Logger log = LoggerFactory.getLogger(RagAdvisor.class);

    private static final String RAG_CONTEXT_TEMPLATE = """
            ---
            Reference material:
            Use the following material when it is relevant. If it is insufficient,
            say that the available context does not contain enough evidence.

            %s
            ---
            """;

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
        return 400;
    }

    @Override
    public AdvisorRequest adviseRequest(AdvisorRequest request, AdvisorContext context) {
        if (retrievalService == null || request == null || isBlank(request.getUserMessage())) {
            return request;
        }

        try {
            String searchQuery = queryRewritingEnabled
                    ? rewriteQuery(request.getUserMessage())
                    : request.getUserMessage();
            if (!searchQuery.equals(request.getUserMessage())) {
                log.debug("RAG query rewritten: '{}' -> '{}'", request.getUserMessage(), searchQuery);
            }

            List<EmbeddingMatch<TextSegment>> results =
                    retrievalService.search(searchQuery, maxRetrievedDocs, minScore);
            context.put(AdvisorContext.RETRIEVED_DOCUMENTS, results);

            String ragContext = formatRetrievedResults(results);
            if (!ragContext.isBlank()) {
                String systemPrompt = request.getSystemPrompt() == null ? "" : request.getSystemPrompt();
                request.setSystemPrompt(systemPrompt + "\n\n" + String.format(RAG_CONTEXT_TEMPLATE, ragContext));
            }

            log.info("RAG retrieval completed: query='{}', results={}", searchQuery, results.size());
        } catch (Exception ex) {
            log.warn("RAG retrieval skipped: {}", ex.getMessage(), ex);
        }

        return request;
    }

    @Override
    public AdvisorResponse adviseResponse(AdvisorResponse response, AdvisorContext context) {
        Object documents = context.get(AdvisorContext.RETRIEVED_DOCUMENTS);
        if (documents instanceof List<?> list) {
            response.setMetadata("rag_sources_count", list.size());
        }
        return response;
    }

    private String rewriteQuery(String originalQuery) {
        if (originalQuery == null) {
            return "";
        }
        return originalQuery
                .replaceFirst("^(我想知道|请问|帮我查一下|告诉我)\\s*", "")
                .replaceFirst("(吗|呢|吧|啊|呀|？|\\?)$", "")
                .trim();
    }

    private String formatRetrievedResults(List<EmbeddingMatch<TextSegment>> results) {
        if (results == null || results.isEmpty()) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < results.size(); i++) {
            EmbeddingMatch<TextSegment> match = results.get(i);
            TextSegment segment = match.embedded();
            builder.append("[").append(i + 1).append("] ");
            builder.append(segment == null ? "" : segment.text());
            builder.append(" (score: ").append(String.format("%.2f", match.score())).append(")");
            if (segment != null && segment.metadata() != null) {
                builder.append(" metadata=").append(segment.metadata());
            }
            builder.append("\n\n");
        }
        return builder.toString();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
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
