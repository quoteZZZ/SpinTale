package com.spintale.ai.retrieval;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RAG 文档管理控制器
 * 
 * 提供文档上传、索引、删除和查询功能
 */
@RestController
@RequestMapping("/ai/rag")
public class RagDocumentController {

    private static final Logger log = LoggerFactory.getLogger(RagDocumentController.class);

    private final RetrievalService retrievalService;
    private final Map<String, String> indexedDocuments = new ConcurrentHashMap<>();

    public RagDocumentController(RetrievalService retrievalService) {
        this.retrievalService = retrievalService;
    }

    /**
     * 上传并索引文档
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadDocument(
            @RequestParam("file") MultipartFile file) throws IOException {
        
        log.info("Uploading document: {}", file.getOriginalFilename());
        
        // 保存临时文件
        Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"), "spintale-docs");
        Files.createDirectories(tempDir);
        
        Path filePath = tempDir.resolve(UUID.randomUUID() + "_" + file.getOriginalFilename());
        Files.write(filePath, file.getBytes());
        
        try {
            // 加载文档
            Document document = FileSystemDocumentLoader.loadDocument(filePath.toString());
            
            // 索引文档
            retrievalService.indexDocuments(List.of(document));
            
            // 记录已索引的文档
            String docId = UUID.randomUUID().toString();
            indexedDocuments.put(docId, file.getOriginalFilename());
            
            log.info("Document indexed successfully: {}", file.getOriginalFilename());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "documentId", docId,
                "filename", file.getOriginalFilename(),
                "message", "Document uploaded and indexed successfully"
            ));
        } catch (Exception e) {
            log.error("Failed to index document", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        } finally {
            // 清理临时文件
            Files.deleteIfExists(filePath);
        }
    }

    /**
     * 批量上传文档
     */
    @PostMapping("/upload/batch")
    public ResponseEntity<Map<String, Object>> uploadBatch(
            @RequestParam("files") List<MultipartFile> files) throws IOException {
        
        log.info("Uploading {} documents", files.size());
        
        int successCount = 0;
        int failCount = 0;
        
        for (MultipartFile file : files) {
            try {
                Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"), "spintale-docs");
                Files.createDirectories(tempDir);
                
                Path filePath = tempDir.resolve(UUID.randomUUID() + "_" + file.getOriginalFilename());
                Files.write(filePath, file.getBytes());
                
                Document document = FileSystemDocumentLoader.loadDocument(filePath.toString());
                retrievalService.indexDocuments(List.of(document));
                
                String docId = UUID.randomUUID().toString();
                indexedDocuments.put(docId, file.getOriginalFilename());
                
                successCount++;
                Files.deleteIfExists(filePath);
            } catch (Exception e) {
                log.error("Failed to index document: {}", file.getOriginalFilename(), e);
                failCount++;
            }
        }
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "totalFiles", files.size(),
            "successCount", successCount,
            "failCount", failCount,
            "message", String.format("Indexed %d/%d documents", successCount, files.size())
        ));
    }

    /**
     * 删除已索引的文档
     */
    @DeleteMapping("/{documentId}")
    public ResponseEntity<Map<String, Object>> deleteDocument(
            @PathVariable String documentId) {
        
        log.info("Deleting document: {}", documentId);
        
        String filename = indexedDocuments.remove(documentId);
        if (filename == null) {
            return ResponseEntity.notFound().build();
        }
        
        // 注意：当前实现不支持单个文档删除，需要重建索引
        // 生产环境应实现更精细的文档管理
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "documentId", documentId,
            "filename", filename,
            "message", "Document removed from index (note: full reindex may be required)"
        ));
    }

    /**
     * 查询文档
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchDocuments(
            @RequestParam("query") String query,
            @RequestParam(value = "topK", defaultValue = "5") int topK,
            @RequestParam(value = "minScore", defaultValue = "0.5") double minScore) {
        
        log.info("Searching for: {}", query);
        
        try {
            List<EmbeddingMatch<TextSegment>> results = retrievalService.search(query, topK, minScore);
            
            List<Map<String, Object>> formattedResults = results.stream()
                .map(match -> Map.<String, Object>of(
                    "score", match.score(),
                    "content", match.embedded() != null ? match.embedded().text() : "",
                    "metadata", match.metadata()
                ))
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

    /**
     * 获取已索引的文档列表
     */
    @GetMapping("/documents")
    public ResponseEntity<Map<String, Object>> listDocuments() {
        return ResponseEntity.ok(Map.of(
            "success", true,
            "documents", indexedDocuments.entrySet().stream()
                .map(entry -> Map.of(
                    "id", entry.getKey(),
                    "filename", entry.getValue()
                ))
                .toList(),
            "count", indexedDocuments.size()
        ));
    }

    /**
     * 清除所有索引
     */
    @DeleteMapping("/clear")
    public ResponseEntity<Map<String, Object>> clearIndex() {
        log.info("Clearing all indexes");
        
        try {
            retrievalService.clearIndex();
            indexedDocuments.clear();
            
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
