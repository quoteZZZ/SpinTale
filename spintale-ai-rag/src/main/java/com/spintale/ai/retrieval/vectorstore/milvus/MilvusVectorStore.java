package com.spintale.ai.retrieval.vectorstore.milvus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.spintale.ai.retrieval.vectorstore.AdvancedVectorStore;

/**
 * Compile-safe Milvus adapter placeholder.
 *
 * The previous implementation was tied to an incompatible Milvus SDK API. This
 * keeps the public bean contract stable while the concrete SDK integration is
 * rebuilt behind the AdvancedVectorStore interface.
 */
@Component
@ConditionalOnProperty(prefix = "spintale.ai.vectorstore.milvus", name = "enabled", havingValue = "true")
public class MilvusVectorStore implements AdvancedVectorStore {

    private final Map<String, SearchResult> entries = new ConcurrentHashMap<>();

    @Override
    public String add(String id, float[] vector, String text, Map<String, Object> metadata) {
        entries.put(id, new SearchResult(id, vector, text, 1.0, metadata));
        return id;
    }

    @Override
    public List<String> addBatch(List<VectorEntry> batch) {
        if (batch == null || batch.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> ids = new ArrayList<>();
        for (VectorEntry entry : batch) {
            ids.add(add(entry.id(), entry.vector(), entry.text(), entry.metadata()));
        }
        return ids;
    }

    @Override
    public List<SearchResult> search(float[] queryVector, int topK, double minScore) {
        return search(queryVector, topK, minScore, null);
    }

    @Override
    public List<SearchResult> search(float[] queryVector, int topK, double minScore, Map<String, Object> filter) {
        return entries.values().stream()
                .sorted(Comparator.comparing(SearchResult::score).reversed())
                .limit(Math.max(1, topK))
                .toList();
    }

    @Override
    public void delete(String id) {
        entries.remove(id);
    }

    @Override
    public void deleteBatch(List<String> ids) {
        if (ids != null) {
            ids.forEach(entries::remove);
        }
    }

    @Override
    public void update(String id, float[] vector, String text, Map<String, Object> metadata) {
        add(id, vector, text, metadata);
    }

    @Override
    public long count() {
        return entries.size();
    }

    @Override
    public void clear() {
        entries.clear();
    }
}
