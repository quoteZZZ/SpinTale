package com.spintale.ai.web.controller;

import com.spintale.ai.retrieval.ingestion.DocumentIndexService;
import com.spintale.common.annotation.Log;
import com.spintale.common.core.domain.AjaxResult;
import com.spintale.common.enums.BusinessType;
import com.spintale.common.utils.SecurityUtils;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.io.IOException;
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

    @PreAuthorize("@ss.hasPermi('ai:rag:upload')")
    @Log(title = "AI文档管理", businessType = BusinessType.INSERT)
    @PostMapping("/upload")
    public AjaxResult uploadDocument(@RequestParam("file") MultipartFile file) throws IOException {
        
        String filename = file.getOriginalFilename();
        log.info("用户[{}]上传文档: {}", SecurityUtils.getUsername(), filename);

        try {
            DocumentIndexService.IndexedDocument indexed =
                    indexService.index(filename, file.getBytes());

            log.info("文档索引成功: {}, ID: {}", indexed.filename(), indexed.id());
            
            return AjaxResult.success("文档上传成功", Map.of(
                "documentId", indexed.id(),
                "filename", indexed.filename(),
                "indexedAt", indexed.indexedAt()
            ));
        } catch (Exception e) {
            log.error("文档索引失败: {}", filename, e);
            return AjaxResult.error("文档上传失败：" + e.getMessage());
        }
    }

    @PreAuthorize("@ss.hasPermi('ai:rag:upload')")
    @Log(title = "AI文档管理", businessType = BusinessType.INSERT)
    @PostMapping("/upload/batch")
    public AjaxResult uploadBatch(@RequestParam("files") List<MultipartFile> files) throws IOException {
        
        log.info("用户[{}]批量上传{}个文档", SecurityUtils.getUsername(), files.size());

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

        return AjaxResult.success(String.format("成功索引%d/%d个文档", result.successCount(), result.total()), Map.of(
            "totalFiles", result.total(),
            "successCount", result.successCount(),
            "failCount", result.failCount(),
            "documents", result.documents(),
            "errors", result.errors()
        ));
    }

    @PreAuthorize("@ss.hasPermi('ai:rag:delete')")
    @Log(title = "AI文档管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{documentId}")
    public AjaxResult deleteDocument(@PathVariable String documentId) {
        
        log.info("用户[{}]删除文档: {}", SecurityUtils.getUsername(), documentId);
        
        boolean removed = indexService.forget(documentId);
        if (!removed) {
            return AjaxResult.error("文档不存在");
        }

        return AjaxResult.success("文档删除成功", Map.of("documentId", documentId));
    }

    @PreAuthorize("@ss.hasPermi('ai:rag:search')")
    @GetMapping("/search")
    public AjaxResult searchDocuments(
            @RequestParam("query") String query,
            @RequestParam(value = "topK", defaultValue = "5") int topK,
            @RequestParam(value = "minScore", defaultValue = "0.5") double minScore) {
        
        log.info("用户[{}]搜索文档: {}", SecurityUtils.getUsername(), query);
        
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
            
            return AjaxResult.success("搜索完成", Map.of(
                "query", query,
                "results", formattedResults,
                "count", formattedResults.size()
            ));
        } catch (Exception e) {
            log.error("搜索失败: {}", query, e);
            return AjaxResult.error("搜索失败：" + e.getMessage());
        }
    }

    @PreAuthorize("@ss.hasPermi('ai:rag:list')")
    @GetMapping("/documents")
    public AjaxResult listDocuments() {
        List<DocumentIndexService.IndexedDocument> documents = indexService.listDocuments();
        return AjaxResult.success(Map.of(
            "documents", documents,
            "count", documents.size()
        ));
    }

    @PreAuthorize("@ss.hasPermi('ai:rag:clear')")
    @Log(title = "AI文档管理", businessType = BusinessType.CLEAN)
    @DeleteMapping("/clear")
    public AjaxResult clearIndex() {
        log.info("用户[{}]清空所有索引", SecurityUtils.getUsername());
        
        try {
            indexService.clear();
            return AjaxResult.success("所有索引已清空");
        } catch (Exception e) {
            log.error("清空索引失败", e);
            return AjaxResult.error("清空索引失败：" + e.getMessage());
        }
    }
}
