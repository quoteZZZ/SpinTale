package com.spintale.ai.retrieval.vectorstore;

import java.util.List;

public interface AdvancedVectorStore {
    
    String add(String id, float[] vector, String text, java.util.Map<String, Object> metadata);
    
    List<String> addBatch(List<VectorEntry> entries);
    
    List<SearchResult> search(float[] queryVector, int topK, double minScore);
    
    List<SearchResult> search(float[] queryVector, int topK, double minScore, java.util.Map<String, Object> filter);
    
    void delete(String id);
    
    void deleteBatch(List<String> ids);
    
    void update(String id, float[] vector, String text, java.util.Map<String, Object> metadata);
    
    long count();
    
    void clear();
    
    record VectorEntry(String id, float[] vector, String text, java.util.Map<String, Object> metadata) {}
    
    record SearchResult(String id, float[] vector, String text, double score, java.util.Map<String, Object> metadata) {}
}
