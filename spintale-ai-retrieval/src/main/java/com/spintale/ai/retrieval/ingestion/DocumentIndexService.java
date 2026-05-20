package com.spintale.ai.retrieval.ingestion;

import com.spintale.ai.retrieval.vector.RetrievalService;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@ConditionalOnBean(RetrievalService.class)
public class DocumentIndexService {

    private final RetrievalService retrievalService;
    private final DocumentParserRegistry parserRegistry;
    private final Map<String, IndexedDocument> indexedDocuments = new ConcurrentHashMap<>();

    public DocumentIndexService(RetrievalService retrievalService, DocumentParserRegistry parserRegistry) {
        this.retrievalService = retrievalService;
        this.parserRegistry = parserRegistry;
    }

    public IndexedDocument index(String filename, byte[] content) throws IOException {
        if (content == null || content.length == 0) {
            throw new IllegalArgumentException("Document content is empty");
        }

        String safeFilename = sanitizeFilename(filename);
        List<Document> documents = parseDocuments(safeFilename, content);
        retrievalService.indexDocuments(documents);

        IndexedDocument indexed = new IndexedDocument(
                UUID.randomUUID().toString(),
                safeFilename,
                System.currentTimeMillis());
        indexedDocuments.put(indexed.id(), indexed);
        return indexed;
    }

    public BatchResult indexAll(List<UploadDocument> documents) {
        List<IndexedDocument> indexed = new ArrayList<>();
        List<IndexError> errors = new ArrayList<>();

        if (documents == null || documents.isEmpty()) {
            return new BatchResult(0, 0, 0, indexed, errors);
        }

        for (UploadDocument document : documents) {
            try {
                indexed.add(index(document.filename(), document.content()));
            } catch (Exception ex) {
                errors.add(new IndexError(document.filename(), ex.getMessage()));
            }
        }

        return new BatchResult(
                documents.size(),
                indexed.size(),
                errors.size(),
                List.copyOf(indexed),
                List.copyOf(errors));
    }

    public List<EmbeddingMatch<TextSegment>> search(String query, int topK, double minScore) {
        return retrievalService.search(query, Math.max(1, topK), minScore);
    }

    public boolean forget(String documentId) {
        return indexedDocuments.remove(documentId) != null;
    }

    public List<IndexedDocument> listDocuments() {
        return indexedDocuments.values().stream()
                .sorted(Comparator.comparing(IndexedDocument::indexedAt).reversed())
                .toList();
    }

    public void clear() {
        retrievalService.clearIndex();
        indexedDocuments.clear();
    }

    private Path createTempFile(String filename, byte[] content) throws IOException {
        Path tempDir = Path.of(System.getProperty("java.io.tmpdir"), "spintale-docs");
        Files.createDirectories(tempDir);
        Path tempFile = Files.createTempFile(tempDir, "doc-", "-" + filename);
        Files.write(tempFile, content);
        return tempFile;
    }

    private List<Document> parseDocuments(String filename, byte[] content) throws IOException {
        DocumentMetadata metadata = new DocumentMetadata("upload", filename);
        var parser = parserRegistry.findParser(filename);
        if (parser.isPresent()) {
            return parser.get().parse(new ByteArrayInputStream(content), metadata);
        }
        Path tempFile = createTempFile(filename, content);
        try {
            return List.of(FileSystemDocumentLoader.loadDocument(tempFile.toString()));
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    private String sanitizeFilename(String filename) {
        String value = filename == null || filename.isBlank() ? "document" : Path.of(filename).getFileName().toString();
        value = value.replaceAll("[\\\\/:*?\"<>|\\p{Cntrl}]", "_").trim();
        return value.isEmpty() ? "document" : value;
    }

    public record UploadDocument(String filename, byte[] content) {
    }

    public record IndexedDocument(String id, String filename, long indexedAt) {
    }

    public record IndexError(String filename, String error) {
    }

    public record BatchResult(
            int total,
            int successCount,
            int failCount,
            List<IndexedDocument> documents,
            List<IndexError> errors) {
    }
}
