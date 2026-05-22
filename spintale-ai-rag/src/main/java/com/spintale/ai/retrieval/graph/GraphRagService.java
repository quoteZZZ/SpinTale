package com.spintale.ai.retrieval.graph;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * GraphRAG 服务 - 基于知识图谱的检索增强生成
 * 支持实体抽取、关系构建、图遍历查询
 */
@Slf4j
@Service
@ConditionalOnProperty(prefix = "spintale.ai.rag.graph", name = "enabled", havingValue = "true")
public class GraphRagService {

    // 内存图谱存储（生产环境应使用 Neo4j/NebulaGraph）
    private final Map<String, Entity> entities = new ConcurrentHashMap<>();
    private final Map<String, List<Relationship>> relationships = new ConcurrentHashMap<>();

    /**
     * 从文档中提取实体和关系构建图谱
     * @param documentId 文档 ID
     * @param content 文档内容
     */
    public void buildGraphFromDocument(String documentId, String content) {
        log.info("从文档构建图谱：{}", documentId);
        
        // 简化版：基于规则的实体抽取
        // 生产环境应使用 NLP 模型（如 spaCy, HanLP）或 LLM 提取
        
        // 1. 提取人名、地名、组织名等
        extractEntities(documentId, content);
        
        // 2. 提取关系
        extractRelationships(documentId, content);
        
        log.info("图谱构建完成：{} 个实体，{} 个关系", entities.size(), relationships.size());
    }

    /**
     * 提取实体（简化实现）
     */
    private void extractEntities(String docId, String content) {
        // 示例：提取引号中的专有名词
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\"([^\"]+)\"");
        var matcher = pattern.matcher(content);
        
        while (matcher.find()) {
            String entityName = matcher.group(1);
            if (entityName.length() > 1 && entityName.length() < 50) {
                entities.computeIfAbsent(entityName, k -> 
                    new Entity(k, "CONCEPT", docId, System.currentTimeMillis())
                );
            }
        }
    }

    /**
     * 提取关系（简化实现）
     */
    private void extractRelationships(String docId, String content) {
        // 示例：检测"是"、"属于"、"位于"等关系词
        String[] relationPatterns = {"是", "属于", "位于", "包含", "创建", "开发"};
        
        for (String pattern : relationPatterns) {
            if (content.contains(pattern)) {
                // 简化：将整段文本作为关系上下文
                String[] parts = content.split(pattern);
                if (parts.length >= 2) {
                    String subject = extractNearestEntity(parts[0]);
                    String object = extractNearestEntity(parts[1]);
                    
                    if (subject != null && object != null) {
                        String relKey = subject + "->" + object;
                        relationships.computeIfAbsent(relKey, k -> new ArrayList<>())
                            .add(new Relationship(subject, object, pattern, docId, System.currentTimeMillis()));
                    }
                }
            }
        }
    }

    /**
     * 从文本片段中提取最近的实体
     */
    private String extractNearestEntity(String text) {
        if (text == null || text.isEmpty()) return null;
        // 简化：返回第一个匹配的实体
        for (String entityName : entities.keySet()) {
            if (text.contains(entityName)) {
                return entityName;
            }
        }
        // 如果没找到，返回截断的文本
        return text.trim().substring(0, Math.min(30, text.length()));
    }

    /**
     * 图谱遍历查询 - 多跳查询
     * @param startEntity 起始实体
     * @param hops 跳数
     * @return 相关实体和关系路径
     */
    public GraphQueryResult traverse(String startEntity, int hops) {
        log.info("图谱遍历查询：起点={}, 跳数={}", startEntity, hops);
        
        if (!entities.containsKey(startEntity)) {
            return new GraphQueryResult(false, "实体不存在", Collections.emptyList(), Collections.emptyList());
        }

        Set<String> visited = new HashSet<>();
        List<String> path = new ArrayList<>();
        List<Relationship> foundRelationships = new ArrayList<>();
        
        Queue<String> queue = new LinkedList<>();
        queue.offer(startEntity);
        visited.add(startEntity);
        path.add(startEntity);

        int currentHop = 0;
        while (!queue.isEmpty() && currentHop < hops) {
            int size = queue.size();
            for (int i = 0; i < size; i++) {
                String current = queue.poll();
                
                // 查找所有与当前实体相关的关系
                for (Map.Entry<String, List<Relationship>> entry : relationships.entrySet()) {
                    for (Relationship rel : entry.getValue()) {
                        if (rel.subject().equals(current) && !visited.contains(rel.object())) {
                            visited.add(rel.object());
                            queue.offer(rel.object());
                            foundRelationships.add(rel);
                            path.add(rel.object());
                        } else if (rel.object().equals(current) && !visited.contains(rel.subject())) {
                            visited.add(rel.subject());
                            queue.offer(rel.subject());
                            foundRelationships.add(rel);
                            path.add(rel.subject());
                        }
                    }
                }
            }
            currentHop++;
        }

        log.info("图谱遍历完成：找到 {} 个关系，路径长度={}", foundRelationships.size(), path.size());
        return new GraphQueryResult(true, "查询成功", path, foundRelationships);
    }

    /**
     * 混合检索：结合向量检索和图谱遍历
     * @param query 查询文本
     * @param vectorResults 向量检索结果
     * @return 增强后的检索结果
     */
    public List<EnhancedChunk> hybridSearch(String query, List<String> vectorResults) {
        log.info("GraphRAG 混合检索：query={}, 向量结果数={}", query, vectorResults.size());
        
        List<EnhancedChunk> enhanced = new ArrayList<>();
        
        // 1. 从查询中提取关键实体
        Set<String> queryEntities = extractQueryEntities(query);
        
        // 2. 对每个向量检索结果进行图谱扩展
        for (String chunkId : vectorResults) {
            EnhancedChunk chunk = new EnhancedChunk(chunkId, new ArrayList<>(), new ArrayList<>());
            
            // 查找与该 chunk 相关的实体
            for (String entity : queryEntities) {
                if (entities.containsKey(entity)) {
                    chunk.relatedEntities().add(entity);
                    
                    // 遍历 1 跳关系
                    GraphQueryResult result = traverse(entity, 1);
                    chunk.relatedPaths().addAll(result.paths());
                }
            }
            
            enhanced.add(chunk);
        }
        
        log.info("混合检索完成：增强 {} 个片段", enhanced.size());
        return enhanced;
    }

    /**
     * 从查询中提取实体
     */
    private Set<String> extractQueryEntities(String query) {
        Set<String> entitiesSet = new HashSet<>();
        // 简化：直接匹配已知实体
        for (String entity : entities.keySet()) {
            if (query.contains(entity)) {
                entitiesSet.add(entity);
            }
        }
        return entitiesSet;
    }

    /**
     * 获取图谱统计信息
     */
    public GraphStats getStats() {
        return new GraphStats(entities.size(), relationships.values().stream().mapToInt(List::size).sum());
    }

    // ==================== 数据模型 ====================

    public record Entity(String name, String type, String sourceDocId, long createdAt) {}
    
    public record Relationship(String subject, String object, String predicate, 
                               String sourceDocId, long createdAt) {}
    
    public record GraphQueryResult(boolean success, String message, 
                                   List<String> paths, List<Relationship> relationships) {}
    
    public record EnhancedChunk(String chunkId, List<String> relatedEntities, 
                                List<String> relatedPaths) {}
    
    public record GraphStats(int entityCount, int relationshipCount) {}
}
