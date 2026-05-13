package com.spintale.ai.retrieval.parser;

import dev.langchain4j.data.document.Document;
import java.io.InputStream;
import java.util.List;

/**
 * 文档解析器接口
 * 支持多种文档格式
 */
public interface DocumentParser {
    
    /**
     * 解析文档
     * @param inputStream 文档输入流
     * @param metadata 元数据（文件名、来源等）
     * @return 解析后的文档列表
     */
    List<Document> parse(InputStream inputStream, DocumentMetadata metadata);
    
    /**
     * 支持的文档格式
     */
    String[] getSupportedFormats();
}
