package com.spintale.ai.web.controller;

import com.spintale.ai.retrieval.ingestion.DocumentIndexService;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * RAG document indexing and search API.
 */
@RestController
@RequestMapping("/ai/rag")
@ConditionalOnBean(DocumentIndexService.class)
public class RagDocumentController {

    private static final Logger log = LoggerFactory.getLogger(RagDocumentController.class);

    private final DocumentIndexService indexService;

    public RagDocumentController(DocumentIndexService indexService) {
        this.indexService = indexService;
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadDocument(
            @RequestParam("file") MultipartFile file) throws IOException {
        
        log.info("Uploading document: {}", file.getOriginalFilename());

        try {
            DocumentIndexService.IndexedDocument indexed =
                    indexService.index(file.getOriginalFilename(), file.getBytes());

            log.info("Document indexed successfully: {}", indexed.filename());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "documentId", indexed.id(),
                "filename", indexed.filename(),
                "indexedAt", indexed.indexedAt(),
                "message", "Document uploaded and indexed successfully"
            ));
        } catch (Exception e) {
            log.error("Failed to index document", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    @PostMapping("/upload/batch")
    public ResponseEntity<Map<String, Object>> uploadBatch(
            @RequestParam("files") List<MultipartFile> files) throws IOException {
        
        log.info("Uploading {} documents", files.size());

        List<DocumentIndexService.UploadDocument> documents = new ArrayList<>();
        for (MultipartFile file : files) {
            byte[] content;
            try {
                content = file.getBytes();
            } catch (IOException ex) {
                content = null;
            }
            documents.add(new DocumentIndexService.UploadDocument(file.getOriginalFilename(), content));
        }
        DocumentIndexService.BatchResult result = indexService.indexAll(documents);

        return ResponseEntity.ok(Map.of(
            "success", true,
            "totalFiles", result.total(),
            "successCount", result.successCount(),
            "failCount", result.failCount(),
            "documents", result.documents(),
            "errors", result.errors(),
            "message", String.format("Indexed %d/%d documents", result.successCount(), result.total())
        ));
    }

    @DeleteMapping("/{documentId}")
    public ResponseEntity<Map<String, Object>> deleteDocument(
            @PathVariable String documentId) {
        
        log.info("Deleting document: {}", documentId);
        
        boolean removed = indexService.forget(documentId);
        if (!removed) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(Map.of(
            "success", true,
            "documentId", documentId,
            "message", "Document metadata removed; rebuild the vector index for physical deletion"
        ));
    }

    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchDocuments(
            @RequestParam("query") String query,
            @RequestParam(value = "topK", defaultValue = "5") int topK,
            @RequestParam(value = "minScore", defaultValue = "0.5") double minScore) {
        
        log.info("Searching for: {}", query);
        
        try {
            List<EmbeddingMatch<TextSegment>> results = indexService.search(query, topK, minScore);
            
            List<Map<String, Object>> formattedResults = results.stream()
                .map(match -> {
                    Map<String, Object> result = new java.util.HashMap<>();
                    result.put("score", match.score());
                    result.put("content", match.embedded() != null ? match.embedded().text() : "");
                    result.put("metadata", match.embedded() != null ? match.embedded().metadata() : null);
                    return result;
                })
                .toList();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "query", query,
                "results", formattedResults,
                "count", formattedResults.size()
            ));
        } catch (Exception e) {
            log.error("Search failed", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/documents")
    public ResponseEntity<Map<String, Object>> listDocuments() {
        List<DocumentIndexService.IndexedDocument> documents = indexService.listDocuments();
        return ResponseEntity.ok(Map.of(
            "success", true,
            "documents", documents,
            "count", documents.size()
        ));
    }

    @DeleteMapping("/clear")
    public ResponseEntity<Map<String, Object>> clearIndex() {
        log.info("Clearing all indexes");
        
        try {
            indexService.clear();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "All indexes cleared"
            ));
        } catch (Exception e) {
            log.error("Failed to clear index", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
}
