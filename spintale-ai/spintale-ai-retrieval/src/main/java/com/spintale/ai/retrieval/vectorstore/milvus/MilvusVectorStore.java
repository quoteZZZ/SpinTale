package com.spintale.ai.retrieval.vectorstore.milvus;

import com.spintale.ai.retrieval.vectorstore.AdvancedVectorStore;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.collection.request.*;
import io.milvus.v2.service.vector.request.*;
import io.milvus.v2.service.vector.response.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(prefix = "spintale.ai.vectorstore.milvus", name = "enabled", havingValue = "true")
public class MilvusVectorStore implements AdvancedVectorStore {
    
    private static final Logger log = LoggerFactory.getLogger(MilvusVectorStore.class);
    
    private final MilvusClientV2 client;
    private final String collectionName;
    private final int dimension;
    
    @Value("${spintale.ai.vectorstore.milvus.metric-type:COSINE}")
    private String metricType = "COSINE";
    
    public MilvusVectorStore(
            MilvusClientV2 client,
            @Value("${spintale.ai.vectorstore.milvus.collection-name:spintale_vectors}") String collectionName,
            @Value("${spintale.ai.vectorstore.milvus.dimension:1536}") int dimension) {
        this.client = client;
        this.collectionName = collectionName;
        this.dimension = dimension;
        
        try {
            createCollectionIfNotExists();
        } catch (Exception e) {
            log.warn("Failed to create collection: {}", e.getMessage());
        }
    }
    
    private void createCollectionIfNotExists() {
        CreateCollectionReq.CollectionSchema schema = CreateCollectionReq.CollectionSchema.builder()
                .fieldTypes(Arrays.asList(
                        io.milvus.v2.common FieldType.builder()
                                .name("id")
                                .dataType(io.milvus.v2.common.DataType.VarChar)
                                .maxLength(256)
                                .isPrimaryKey(true)
                                .build(),
                        io.milvus.v2.common FieldType.builder()
                                .name("vector")
                                .dataType(io.milvus.v2.common.DataType.FloatVector)
                                .dimension(dimension)
                                .build(),
                        io.milvus.v2.common FieldType.builder()
                                .name("text")
                                .dataType(io.milvus.v2.common.DataType.VarChar)
                                .maxLength(65535)
                                .build()
                ))
                .build();
        
        CreateCollectionReq request = CreateCollectionReq.builder()
                .collectionName(collectionName)
                .collectionSchema(schema)
                .build();
        
        client.createCollection(request);
        log.info("Milvus collection created: {}", collectionName);
    }
    
    @Override
    public String add(String id, float[] vector, String text, Map<String, Object> metadata) {
        try {
            JsonObject data = new JsonObject();
            data.addProperty("id", id);
            data.add("vector", gson.toJsonTree(vector));
            data.addProperty("text", text);
            
            InsertReq request = InsertReq.builder()
                    .collectionName(collectionName)
                    .data(Collections.singletonList(data))
                    .build();
            
            client.insert(request);
            log.debug("Added vector to Milvus: id={}", id);
            return id;
        } catch (Exception e) {
            log.error("Failed to add vector to Milvus: {}", e.getMessage());
            throw new RuntimeException("Failed to add vector", e);
        }
    }
    
    @Override
    public List<String> addBatch(List<VectorEntry> entries) {
        if (entries == null || entries.isEmpty()) {
            return Collections.emptyList();
        }
        
        try {
            List<JsonObject> dataList = entries.stream()
                    .map(entry -> {
                        JsonObject data = new JsonObject();
                        data.addProperty("id", entry.id());
                        data.add("vector", gson.toJsonTree(entry.vector()));
                        data.addProperty("text", entry.text());
                        return data;
                    })
                    .collect(Collectors.toList());
            
            InsertReq request = InsertReq.builder()
                    .collectionName(collectionName)
                    .data(dataList)
                    .build();
            
            client.insert(request);
            
            log.info("Added {} vectors to Milvus", entries.size());
            return entries.stream().map(VectorEntry::id).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to add batch vectors to Milvus: {}", e.getMessage());
            throw new RuntimeException("Failed to add batch vectors", e);
        }
    }
    
    @Override
    public List<SearchResult> search(float[] queryVector, int topK, double minScore) {
        return search(queryVector, topK, minScore, null);
    }
    
    @Override
    public List<SearchResult> search(float[] queryVector, int topK, double minScore, Map<String, Object> filter) {
        try {
            SearchReq.SearchParams searchParams = SearchReq.SearchParams.builder()
                    .annsField("vector")
                    .topK(topK)
                    .metricType(io.milvus.v2.common.MetricType.valueOf(metricType))
                    .build();
            
            SearchReq request = SearchReq.builder()
                    .collectionName(collectionName)
                    .data(Collections.singletonList(queryVector))
                    .searchParams(searchParams)
                    .outputFields(Collections.singletonList("text"))
                    .build();
            
            SearchResp response = client.search(request);
            
            List<SearchResult> results = new ArrayList<>();
            List<SearchResp.SearchResult> searchResults = response.getSearchResults().get(0);
            
            for (SearchResp.SearchResult result : searchResults) {
                double score = result.getScore();
                if (score >= minScore) {
                    String id = result.getId().toString();
                    String text = result.getEntity().get("text").toString();
                    
                    results.add(new SearchResult(id, null, text, score, null));
                }
            }
            
            log.debug("Searched Milvus: querySize={}, topK={}, results={}", 
                    queryVector.length, topK, results.size());
            
            return results;
        } catch (Exception e) {
            log.error("Failed to search Milvus: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
    
    @Override
    public void delete(String id) {
        try {
            DeleteReq request = DeleteReq.builder()
                    .collectionName(collectionName)
                    .ids(Collections.singletonList(id))
                    .build();
            
            client.delete(request);
            log.debug("Deleted vector from Milvus: id={}", id);
        } catch (Exception e) {
            log.error("Failed to delete vector from Milvus: {}", e.getMessage());
        }
    }
    
    @Override
    public void deleteBatch(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        
        try {
            DeleteReq request = DeleteReq.builder()
                    .collectionName(collectionName)
                    .ids(ids)
                    .build();
            
            client.delete(request);
            log.info("Deleted {} vectors from Milvus", ids.size());
        } catch (Exception e) {
            log.error("Failed to delete batch vectors from Milvus: {}", e.getMessage());
        }
    }
    
    @Override
    public void update(String id, float[] vector, String text, Map<String, Object> metadata) {
        delete(id);
        add(id, vector, text, metadata);
    }
    
    @Override
    public long count() {
        try {
            GetCollectionStatisticsReq request = GetCollectionStatisticsReq.builder()
                    .collectionName(collectionName)
                    .build();
            
            GetCollectionStatisticsResp response = client.getCollectionStatistics(request);
            return response.getRowCount();
        } catch (Exception e) {
            log.error("Failed to count vectors in Milvus: {}", e.getMessage());
            return 0;
        }
    }
    
    @Override
    public void clear() {
        try {
            DropCollectionReq request = DropCollectionReq.builder()
                    .collectionName(collectionName)
                    .build();
            
            client.dropCollection(request);
            createCollectionIfNotExists();
            log.info("Cleared Milvus collection: {}", collectionName);
        } catch (Exception e) {
            log.error("Failed to clear Milvus collection: {}", e.getMessage());
        }
    }
    
    private static final com.google.gson.Gson gson = new com.google.gson.Gson();
}
