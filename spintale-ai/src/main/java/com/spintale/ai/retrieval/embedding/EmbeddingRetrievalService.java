package com.spintale.ai.retrieval.embedding;

import java.util.List;
import java.util.stream.Collectors;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingStore;

public class EmbeddingRetrievalService implements RetrievalService
{
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final EmbeddingModel embeddingModel;
    private final DocumentSplitter splitter;

    public EmbeddingRetrievalService(EmbeddingStore<TextSegment> embeddingStore, EmbeddingModel embeddingModel)
    {
        this(embeddingStore, embeddingModel, 800, 120);
    }

    public EmbeddingRetrievalService(EmbeddingStore<TextSegment> embeddingStore, EmbeddingModel embeddingModel,
            int maxSegmentSize, int maxOverlapSize)
    {
        this.embeddingStore = embeddingStore;
        this.embeddingModel = embeddingModel;
        this.splitter = DocumentSplitters.recursive(maxSegmentSize, maxOverlapSize);
    }

    @Override
    public void indexDocuments(List<Document> documents)
    {
        List<TextSegment> segments = documents.stream()
                .flatMap(document -> splitter.split(document).stream())
                .collect(Collectors.toList());
        indexSegments(segments);
    }

    @Override
    public void indexSegments(List<TextSegment> segments)
    {
        for (TextSegment segment : segments)
        {
            embeddingStore.add(embeddingModel.embed(segment).content(), segment);
        }
    }

    @Override
    public List<EmbeddingMatch<TextSegment>> search(String query, int maxResults)
    {
        return search(query, maxResults, 0.0D);
    }

    @Override
    public List<EmbeddingMatch<TextSegment>> search(String query, int maxResults, double minScore)
    {
        EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
                .queryEmbedding(embeddingModel.embed(query).content())
                .maxResults(maxResults)
                .minScore(minScore)
                .build();
        return embeddingStore.search(request).matches();
    }

    @Override
    public void clearIndex()
    {
        embeddingStore.removeAll();
    }
}
