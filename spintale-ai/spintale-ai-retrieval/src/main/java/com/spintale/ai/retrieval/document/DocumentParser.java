package com.spintale.ai.retrieval.document;

import dev.langchain4j.data.document.Document;
import java.io.InputStream;
import java.util.List;

/**
 * Document parser SPI - converts various document formats into LangChain4j documents.
 */
public interface DocumentParser {
    
    /**
     * Parse document from input stream.
     *
     * @param inputStream document content
     * @param metadata document metadata (filename, source, etc.)
     * @return parsed LangChain4j documents
     */
    List<Document> parse(InputStream inputStream, DocumentMetadata metadata);
    
    /**
     * Get supported file formats (e.g., ["pdf", "PDF"]).
     *
     * @return array of supported extensions
     */
    String[] getSupportedFormats();
}
