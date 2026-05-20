package com.spintale.ai.retrieval.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Document metadata for tracking source information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentMetadata {
    
    /** Source URL or file path */
    private String source;
    
    /** Original filename */
    private String filename;
    
    /** Document type (pdf, word, markdown, etc.) */
    private String documentType;
    
    /** Additional custom metadata */
    private java.util.Map<String, String> customMetadata;
}
