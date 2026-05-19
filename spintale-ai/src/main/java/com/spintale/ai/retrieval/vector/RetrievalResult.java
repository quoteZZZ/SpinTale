package com.spintale.ai.retrieval.vector;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Retrieval result container for Temporal workflow.
 * 
 * This class wraps the embedding matches and provides
 * convenient access to document content and metadata.
 * 
 * @author SpinTale AI Team
 */
public class RetrievalResult {
    
    private List<RetrievedDocument> documents;
    private double avgScore;
    private int totalMatches;
    private long searchTimeMs;
    
    public RetrievalResult() {
    }
    
    public RetrievalResult(List<EmbeddingMatch<TextSegment>> matches, long searchTimeMs) {
        this.searchTimeMs = searchTimeMs;
        this.totalMatches = matches.size();
        
        if (matches != null && !matches.isEmpty()) {
            this.documents = matches.stream()
                .map(match -> new RetrievedDocument(
                    match.embedded().text(),
                    match.score(),
                    match.embedded().metadata().toMap()
                ))
                .collect(Collectors.toList());
            
            this.avgScore = matches.stream()
                .mapToDouble(EmbeddingMatch::score)
                .average()
                .orElse(0.0);
        } else {
            this.documents = List.of();
            this.avgScore = 0.0;
        }
    }
    
    /**
     * Get retrieved documents.
     */
    public List<RetrievedDocument> getDocuments() {
        return documents;
    }
    
    public void setDocuments(List<RetrievedDocument> documents) {
        this.documents = documents;
    }
    
    /**
     * Get average relevance score.
     */
    public double getAvgScore() {
        return avgScore;
    }
    
    public void setAvgScore(double avgScore) {
        this.avgScore = avgScore;
    }
    
    /**
     * Get total number of matches.
     */
    public int getTotalMatches() {
        return totalMatches;
    }
    
    public void setTotalMatches(int totalMatches) {
        this.totalMatches = totalMatches;
    }
    
    /**
     * Get search execution time in milliseconds.
     */
    public long getSearchTimeMs() {
        return searchTimeMs;
    }
    
    public void setSearchTimeMs(long searchTimeMs) {
        this.searchTimeMs = searchTimeMs;
    }
    
    /**
     * Check if any documents were retrieved.
     */
    public boolean hasResults() {
        return documents != null && !documents.isEmpty();
    }
    
    /**
     * Get top N documents by score.
     */
    public List<RetrievedDocument> getTopDocuments(int n) {
        if (documents == null || documents.isEmpty()) {
            return List.of();
        }
        return documents.stream()
            .limit(n)
            .collect(Collectors.toList());
    }
    
    /**
     * Get documents with score above threshold.
     */
    public List<RetrievedDocument> getDocumentsAboveThreshold(double threshold) {
        if (documents == null || documents.isEmpty()) {
            return List.of();
        }
        return documents.stream()
            .filter(doc -> doc.getScore() >= threshold)
            .collect(Collectors.toList());
    }
    
    @Override
    public String toString() {
        return "RetrievalResult{" +
            "documents=" + (documents != null ? documents.size() : 0) +
            ", avgScore=" + avgScore +
            ", totalMatches=" + totalMatches +
            ", searchTimeMs=" + searchTimeMs +
            '}';
    }
    
    /**
     * Inner class representing a retrieved document.
     */
    public static class RetrievedDocument {
        private String content;
        private double score;
        private java.util.Map<String, Object> metadata;
        
        public RetrievedDocument() {
        }
        
        public RetrievedDocument(String content, double score, java.util.Map<String, Object> metadata) {
            this.content = content;
            this.score = score;
            this.metadata = metadata;
        }
        
        public String getContent() {
            return content;
        }
        
        public void setContent(String content) {
            this.content = content;
        }
        
        public double getScore() {
            return score;
        }
        
        public void setScore(double score) {
            this.score = score;
        }
        
        public java.util.Map<String, Object> getMetadata() {
            return metadata;
        }
        
        public void setMetadata(java.util.Map<String, Object> metadata) {
            this.metadata = metadata;
        }
        
        @Override
        public String toString() {
            return "RetrievedDocument{" +
                "content='" + (content != null ? content.substring(0, Math.min(50, content.length())) : "null") + "...'" +
                ", score=" + score +
                '}';
        }
    }
}
