package com.spintale.ai.web.controller;

import com.spintale.ai.retrieval.ingestion.DocumentIndexService;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public ResponseEntity<Map<String, Object>> uploadDocument(@RequestParam("file") MultipartFile file)
            throws IOException {
        String filename = file.getOriginalFilename();
        DocumentIndexService.IndexedDocument indexed = indexService.index(filename, file.getBytes());
        log.info("Indexed RAG document: filename={}, id={}", indexed.filename(), indexed.id());

        return ResponseEntity.status(HttpStatus.CREATED).body(success(Map.of(
                "documentId", indexed.id(),
                "filename", indexed.filename(),
                "indexedAt", indexed.indexedAt())));
    }

    @PostMapping("/upload/batch")
    public Map<String, Object> uploadBatch(@RequestParam("files") List<MultipartFile> files) {
        List<DocumentIndexService.UploadDocument> documents = new ArrayList<>();
        for (MultipartFile file : files) {
            byte[] content;
            try {
                content = file.getBytes();
            } catch (IOException ex) {
                content = new byte[0];
            }
            documents.add(new DocumentIndexService.UploadDocument(file.getOriginalFilename(), content));
        }

        DocumentIndexService.BatchResult result = indexService.indexAll(documents);
        return success(Map.of(
                "totalFiles", result.total(),
                "successCount", result.successCount(),
                "failCount", result.failCount(),
                "documents", result.documents(),
                "errors", result.errors()));
    }

    @DeleteMapping("/{documentId}")
    public ResponseEntity<Map<String, Object>> deleteDocument(@PathVariable String documentId) {
        boolean removed = indexService.forget(documentId);
        if (!removed) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error("DOCUMENT_NOT_FOUND", "Document not found"));
        }
        return ResponseEntity.ok(success(Map.of("documentId", documentId)));
    }

    @GetMapping("/search")
    public Map<String, Object> searchDocuments(
            @RequestParam("query") String query,
            @RequestParam(value = "topK", defaultValue = "5") int topK,
            @RequestParam(value = "minScore", defaultValue = "0.5") double minScore) {
        List<EmbeddingMatch<TextSegment>> results = indexService.search(query, topK, minScore);
        List<Map<String, Object>> formattedResults = results.stream()
                .map(this::formatMatch)
                .toList();

        return success(Map.of(
                "query", query,
                "results", formattedResults,
                "count", formattedResults.size()));
    }

    @GetMapping("/documents")
    public Map<String, Object> listDocuments() {
        List<DocumentIndexService.IndexedDocument> documents = indexService.listDocuments();
        return success(Map.of(
                "documents", documents,
                "count", documents.size()));
    }

    @DeleteMapping("/clear")
    public Map<String, Object> clearIndex() {
        indexService.clear();
        return success(Map.of("cleared", true));
    }

    private Map<String, Object> formatMatch(EmbeddingMatch<TextSegment> match) {
        Map<String, Object> result = new HashMap<>();
        TextSegment segment = match.embedded();
        result.put("score", match.score());
        result.put("content", segment == null ? "" : segment.text());
        result.put("metadata", segment == null ? Map.of() : segment.metadata());
        return result;
    }

    private Map<String, Object> success(Map<String, Object> data) {
        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("data", data);
        return body;
    }

    private Map<String, Object> error(String code, String message) {
        return Map.of(
                "success", false,
                "code", code,
                "message", message);
    }
}
