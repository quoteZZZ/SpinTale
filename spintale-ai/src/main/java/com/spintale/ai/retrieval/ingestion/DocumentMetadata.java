package com.spintale.ai.retrieval.ingestion;

import java.util.HashMap;
import java.util.Map;

/**
 * 文档元数据
 */
public class DocumentMetadata {
    
    private final String source;
    private final String filename;
    private final Map<String, Object> customMetadata;
    
    public DocumentMetadata(String source, String filename) {
        this(source, filename, null);
    }
    
    public DocumentMetadata(String source, String filename, Map<String, Object> customMetadata) {
        this.source = source;
        this.filename = filename;
        this.customMetadata = customMetadata != null ?
            new HashMap<>(customMetadata) : new HashMap<>();
    }
    
    public String getSource() {
        return source;
    }
    
    public String getFilename() {
        return filename;
    }
    
    public Map<String, Object> getCustomMetadata() {
        return customMetadata;
    }
    
    public void putCustomMetadata(String key, Object value) {
        customMetadata.put(key, value);
    }
}
