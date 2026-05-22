package com.spintale.ai.retrieval.vector;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;

import java.util.List;

/**
 * Vector retrieval contract used by RAG advisors, APIs, and agent workflows.
 */
public interface RetrievalService {

    void indexDocuments(List<Document> documents);

    void indexSegments(List<TextSegment> segments);

    List<EmbeddingMatch<TextSegment>> search(String query, int maxResults);

    List<EmbeddingMatch<TextSegment>> search(String query, int maxResults, double minScore);

    void clearIndex();
}
