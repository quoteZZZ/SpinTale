package com.spintale.ai.agent.memory.impl;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.spintale.ai.agent.memory.LongTermMemory;
import com.spintale.ai.agent.memory.api.LongTermMemoryManager;

/**
 * 基于内存和向量嵌入的长期记忆管理器实现
 * 支持语义搜索、重要性评分和记忆压缩
 */
public class MemoryLtmManager implements LongTermMemoryManager {
    
    private static final Logger log = LoggerFactory.getLogger(MemoryLtmManager.class);
    
    // 记忆存储（生产环境应使用数据库）
    private final Map<String, LongTermMemory> memoryStore = new ConcurrentHashMap<>();
    
    // 用户记忆索引
    private final Map<String, Set<String>> userMemoryIndex = new ConcurrentHashMap<>();
    
    // 向量存储（用于语义搜索）
    private final EmbeddingStore<LongTermMemory> embeddingStore;
    private final EmbeddingModel embeddingModel;
    
    // AI 模型（用于记忆提取和压缩）
    private final ChatModel chatModel;
    
    // 默认重要性阈值
    private static final double DEFAULT_IMPORTANCE_THRESHOLD = 0.3;
    
    // 最大短期记忆数量（超过后触发压缩）
    private static final int MAX_SHORT_TERM_MEMORIES = 50;
    
    public MemoryLtmManager(
            EmbeddingStore<LongTermMemory> embeddingStore,
            EmbeddingModel embeddingModel,
            ChatModel chatModel) {
        this.embeddingStore = embeddingStore;
        this.embeddingModel = embeddingModel;
        this.chatModel = chatModel;
    }
    
    @Override
    public LongTermMemory addMemory(LongTermMemory memory) {
        if (memory.getId() == null) {
            memory.setId(UUID.randomUUID().toString());
        }
        if (memory.getCreatedAt() == null) {
            memory.setCreatedAt(LocalDateTime.now());
        }
        if (memory.getAccessCount() == null) {
            memory.setAccessCount(0);
        }
        if (memory.getImportanceScore() == null) {
            memory.setImportanceScore(calculateInitialImportance(memory));
        }
        
        // 存储记忆
        memoryStore.put(memory.getId(), memory);
        
        // 更新用户索引
        userMemoryIndex.computeIfAbsent(memory.getUserId(), k -> ConcurrentHashMap.newKeySet())
                .add(memory.getId());
        
        // 添加到向量存储（用于语义搜索）
        try {
            embeddingStore.add(embeddingModel.embed(memory.getContent()).content(), memory);
        } catch (Exception e) {
            log.warn("Failed to add memory to embedding store: {}", e.getMessage());
        }
        
        log.info("Added long-term memory: id={}, userId={}, type={}", 
                memory.getId(), memory.getUserId(), memory.getType());
        
        // 检查是否需要压缩
        checkAndCompressMemories(memory.getUserId());
        
        return memory;
    }
    
    @Override
    public List<LongTermMemory> addMemories(List<LongTermMemory> memories) {
        return memories.parallelStream().map(this::addMemory).collect(Collectors.toList());
    }
    
    @Override
    public LongTermMemory getMemory(String id) {
        LongTermMemory memory = memoryStore.get(id);
        if (memory != null) {
            // 更新访问信息
            memory.setAccessCount(memory.getAccessCount() + 1);
            memory.setLastAccessedAt(LocalDateTime.now());
            
            // 根据访问次数提升重要性
            if (memory.getAccessCount() % 5 == 0 && memory.getImportanceScore() < 0.9) {
                memory.setImportanceScore(Math.min(1.0, memory.getImportanceScore() + 0.05));
            }
        }
        return memory;
    }
    
    @Override
    public List<LongTermMemory> getUserMemories(String userId, int limit) {
        Set<String> memoryIds = userMemoryIndex.get(userId);
        if (memoryIds == null || memoryIds.isEmpty()) {
            return Collections.emptyList();
        }
        
        return memoryIds.stream()
                .map(memoryStore::get)
                .filter(Objects::nonNull)
                .filter(m -> m.getExpiresAt() == null || m.getExpiresAt().isAfter(LocalDateTime.now()))
                .sorted((a, b) -> {
                    // 按重要性和最近访问时间排序
                    int importanceCompare = Double.compare(b.getImportanceScore(), a.getImportanceScore());
                    if (importanceCompare != 0) return importanceCompare;
                    return b.getLastAccessedAt().compareTo(a.getLastAccessedAt());
                })
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<LongTermMemory> getMemoriesByType(String userId, LongTermMemory.MemoryType type, int limit) {
        return getUserMemories(userId, Integer.MAX_VALUE).stream()
                .filter(m -> m.getType() == type)
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<LongTermMemory> searchMemories(String userId, String query, int maxResults, double minScore) {
        // 先进行语义搜索
        EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
                .queryEmbedding(embeddingModel.embed(query).content())
                .maxResults(maxResults * 2) // 获取更多结果以便过滤
                .minScore(minScore)
                .build();
        
        List<EmbeddingMatch<LongTermMemory>> matches;
        try {
            matches = embeddingStore.search(request).matches();
        } catch (Exception e) {
            log.error("Semantic search failed: {}", e.getMessage());
            return Collections.emptyList();
        }
        
        // 过滤属于该用户的记忆，并按相关性排序
        return matches.stream()
                .map(EmbeddingMatch::embedded)
                .filter(m -> m.getUserId().equals(userId))
                .filter(m -> m.getExpiresAt() == null || m.getExpiresAt().isAfter(LocalDateTime.now()))
                .sorted((a, b) -> Double.compare(b.getImportanceScore(), a.getImportanceScore()))
                .limit(maxResults)
                .collect(Collectors.toList());
    }
    
    @Override
    public LongTermMemory updateMemory(LongTermMemory memory) {
        if (memory.getId() == null || !memoryStore.containsKey(memory.getId())) {
            throw new IllegalArgumentException("Memory not found: " + memory.getId());
        }
        
        memory.setLastAccessedAt(LocalDateTime.now());
        memoryStore.put(memory.getId(), memory);
        
        // 更新向量存储中的嵌入
        try {
            embeddingStore.remove(memory.getId());
            embeddingStore.add(embeddingModel.embed(memory.getContent()).content(), memory);
        } catch (Exception e) {
            log.warn("Failed to update memory in embedding store: {}", e.getMessage());
        }
        
        return memory;
    }
    
    @Override
    public boolean deleteMemory(String id) {
        LongTermMemory memory = memoryStore.remove(id);
        if (memory != null) {
            userMemoryIndex.computeIfPresent(memory.getUserId(), (k, v) -> {
                v.remove(id);
                return v.isEmpty() ? null : v;
            });
            
            try {
                embeddingStore.remove(id);
            } catch (Exception e) {
                log.warn("Failed to remove memory from embedding store: {}", e.getMessage());
            }
            
            log.info("Deleted memory: id={}", id);
            return true;
        }
        return false;
    }
    
    @Override
    public int deleteMemories(List<String> ids) {
        return ids.parallelStream()
                .mapToInt(id -> deleteMemory(id) ? 1 : 0)
                .sum();
    }
    
    @Override
    public int cleanupExpiredMemories() {
        LocalDateTime now = LocalDateTime.now();
        List<String> expiredIds = memoryStore.values().stream()
                .filter(m -> m.getExpiresAt() != null && m.getExpiresAt().isBefore(now))
                .map(LongTermMemory::getId)
                .collect(Collectors.toList());
        
        return deleteMemories(expiredIds);
    }
    
    @Override
    public LongTermMemory compressMemories(String userId, List<String> memoryIds, String summary) {
        // 创建摘要记忆
        LongTermMemory summaryMemory = new LongTermMemory();
        summaryMemory.setId(UUID.randomUUID().toString());
        summaryMemory.setUserId(userId);
        summaryMemory.setType(LongTermMemory.MemoryType.SUMMARY);
        summaryMemory.setContent(summary);
        summaryMemory.setCreatedAt(LocalDateTime.now());
        summaryMemory.setImportanceScore(0.8); // 摘要通常较重要
        
        // 删除原始记忆
        deleteMemories(memoryIds);
        
        // 添加摘要记忆
        return addMemory(summaryMemory);
    }
    
    @Override
    public LongTermMemory updateImportanceScore(String id, Double newScore) {
        LongTermMemory memory = getMemory(id);
        if (memory != null) {
            memory.setImportanceScore(Math.max(0.0, Math.min(1.0, newScore)));
            memoryStore.put(id, memory);
        }
        return memory;
    }
    
    /**
     * 计算初始重要性评分
     */
    private double calculateInitialImportance(LongTermMemory memory) {
        double baseScore = 0.5;
        
        // 根据类型调整
        switch (memory.getType()) {
            case FACT:
                baseScore += 0.2;
                break;
            case PREFERENCE:
                baseScore += 0.15;
                break;
            case EVENT:
                baseScore += 0.1;
                break;
            case SUMMARY:
                baseScore += 0.3;
                break;
            default:
                break;
        }
        
        // 根据内容长度调整（适中的长度通常更有价值）
        int contentLength = memory.getContent() != null ? memory.getContent().length() : 0;
        if (contentLength > 50 && contentLength < 500) {
            baseScore += 0.1;
        }
        
        return Math.min(1.0, baseScore);
    }
    
    /**
     * 检查并压缩用户的记忆
     */
    private void checkAndCompressMemories(String userId) {
        List<LongTermMemory> memories = getUserMemories(userId, Integer.MAX_VALUE);
        
        // 按类型分组
        Map<LongTermMemory.MemoryType, List<LongTermMemory>> byType = memories.stream()
                .collect(Collectors.groupingBy(LongTermMemory::getType));
        
        // 对每种类型的记忆，如果数量过多则压缩最不重要的
        for (Map.Entry<LongTermMemory.MemoryType, List<LongTermMemory>> entry : byType.entrySet()) {
            List<LongTermMemory> typeMemories = entry.getValue();
            if (typeMemories.size() > MAX_SHORT_TERM_MEMORIES) {
                // 获取最不重要的记忆
                List<String> toCompress = typeMemories.stream()
                        .sorted(Comparator.comparingDouble(LongTermMemory::getImportanceScore))
                        .limit(typeMemories.size() - MAX_SHORT_TERM_MEMORIES / 2)
                        .map(LongTermMemory::getId)
                        .collect(Collectors.toList());
                
                log.info("Compressing {} memories for user {}", toCompress.size(), userId);
                // 这里可以调用 AI 生成摘要
                // compressMemories(userId, toCompress, generatedSummary);
            }
        }
    }
}
