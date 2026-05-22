package com.spintale.ai.retrieval.vector;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;

import java.util.List;

/**
 * Vector store SPI for semantic search and retrieval.
 */
public interface VectorStore {

    /**
     * Index documents by splitting, embedding, and storing.
     *
     * @param documents list of documents to index
     */
    void indexDocuments(List<Document> documents);

    /**
     * Index pre-split text segments.
     *
     * @param segments list of text segments
     */
    void indexSegments(List<TextSegment> segments);

    /**
     * Search for similar segments.
     *
     * @param query search query
     * @param maxResults maximum number of results
     * @return matching segments with similarity scores
     */
    List<EmbeddingMatch<TextSegment>> search(String query, int maxResults);

    /**
     * Search with minimum similarity threshold.
     *
     * @param query search query
     * @param maxResults maximum number of results
     * @param minScore minimum similarity score (0.0-1.0)
     * @return matching segments with similarity scores
     */
    List<EmbeddingMatch<TextSegment>> search(String query, int maxResults, double minScore);

    /**
     * Clear all indexed data.
     */
    void clearIndex();
}
