package com.spintale.ai.retrieval.ingestion;

import dev.langchain4j.data.document.Document;
import java.io.InputStream;
import java.util.List;

/**
 * Converts uploaded document bytes into LangChain4j documents.
 */
public interface DocumentParser {
    
    List<Document> parse(InputStream inputStream, DocumentMetadata metadata);
    
    String[] getSupportedFormats();
}
