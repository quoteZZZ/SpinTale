package com.spintale.ai.retrieval;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 基于 LangChain4j 的 RAG 检索服务实现
 */
@Slf4j
public class EmbeddingRetrievalService implements RetrievalService {
    
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final EmbeddingModel embeddingModel;
    private final DocumentSplitter splitter;
    
    public EmbeddingRetrievalService(
            EmbeddingStore<TextSegment> embeddingStore,
            EmbeddingModel embeddingModel) {
        this(embeddingStore, embeddingModel, 300, 30);
    }
    
    public EmbeddingRetrievalService(
            EmbeddingStore<TextSegment> embeddingStore,
            EmbeddingModel embeddingModel,
            int maxSegmentSize,
            int maxOverlapSize) {
        this.embeddingStore = embeddingStore;
        this.embeddingModel = embeddingModel;
        this.splitter = DocumentSplitters.recursive(maxSegmentSize, maxOverlapSize);
        log.info("Initialized EmbeddingRetrievalService with segmentSize={}, overlapSize={}", 
                maxSegmentSize, maxOverlapSize);
    }
    
    @Override
    public void indexDocuments(List<Document> documents) {
        log.info("Indexing {} documents", documents.size());
        
        // 分割文档为文本片段
        List<TextSegment> segments = documents.stream()
                .flatMap(doc -> splitter.split(doc).stream())
                .collect(Collectors.toList());
        
        indexSegments(segments);
    }
    
    @Override
    public void indexSegments(List<TextSegment> segments) {
        log.info("Indexing {} text segments", segments.size());
        
        // 批量嵌入并存储
        for (TextSegment segment : segments) {
            try {
                var embedding = embeddingModel.embed(segment);
                embeddingStore.add(embedding.content(), segment);
            } catch (Exception e) {
                log.error("Failed to embed segment: {}", e.getMessage());
            }
        }
        
        log.info("Successfully indexed {} segments", segments.size());
    }
    
    @Override
    public List<EmbeddingMatch<TextSegment>> search(String query, int maxResults) {
        return search(query, maxResults, 0.0);
    }
    
    @Override
    public List<EmbeddingMatch<TextSegment>> search(String query, int maxResults, double minScore) {
        log.debug("Searching for: {} (maxResults={}, minScore={})", query, maxResults, minScore);
        
        var embedding = embeddingModel.embed(query);
        List<EmbeddingMatch<TextSegment>> matches = embeddingStore.findRelevant(
                embedding.content(), 
                maxResults, 
                minScore
        );
        
        log.debug("Found {} matches", matches.size());
        return matches;
    }
    
    @Override
    public void clearIndex() {
        log.info("Clearing embedding index");
        embeddingStore.removeAll();
    }
}
