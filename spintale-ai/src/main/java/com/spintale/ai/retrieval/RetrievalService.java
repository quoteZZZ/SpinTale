package com.spintale.ai.retrieval;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;

import java.util.List;

/**
 * RAG 检索服务接口
 */
public interface RetrievalService {
    
    /**
     * 索引文档
     * @param documents 文档列表
     */
    void indexDocuments(List<Document> documents);
    
    /**
     * 索引文本片段
     * @param segments 文本片段列表
     */
    void indexSegments(List<TextSegment> segments);
    
    /**
     * 语义搜索
     * @param query 查询文本
     * @param maxResults 最大返回结果数
     * @return 匹配的嵌入结果
     */
    List<EmbeddingMatch<TextSegment>> search(String query, int maxResults);
    
    /**
     * 语义搜索（带相似度阈值）
     * @param query 查询文本
     * @param maxResults 最大返回结果数
     * @param minScore 最小相似度分数
     * @return 匹配的嵌入结果
     */
    List<EmbeddingMatch<TextSegment>> search(String query, int maxResults, double minScore);
    
    /**
     * 清除索引
     */
    void clearIndex();
}
