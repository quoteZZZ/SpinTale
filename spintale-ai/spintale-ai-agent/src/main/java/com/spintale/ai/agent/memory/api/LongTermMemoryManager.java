package com.spintale.ai.agent.memory.api;

import com.spintale.ai.agent.memory.LongTermMemory;

import java.util.List;

/**
 * 长期记忆管理器接口
 * 负责记忆的存储、检索、更新和遗忘
 */
public interface LongTermMemoryManager {
    
    /**
     * 添加记忆
     * @param memory 记忆对象
     * @return 添加后的记忆（包含生成的 ID）
     */
    LongTermMemory addMemory(LongTermMemory memory);
    
    /**
     * 批量添加记忆
     * @param memories 记忆列表
     * @return 添加后的记忆列表
     */
    List<LongTermMemory> addMemories(List<LongTermMemory> memories);
    
    /**
     * 根据 ID 获取记忆
     * @param id 记忆 ID
     * @return 记忆对象，不存在则返回 null
     */
    LongTermMemory getMemory(String id);
    
    /**
     * 根据用户 ID 获取记忆列表
     * @param userId 用户 ID
     * @param limit 最大返回数量
     * @return 记忆列表
     */
    List<LongTermMemory> getUserMemories(String userId, int limit);
    
    /**
     * 根据类型获取记忆
     * @param userId 用户 ID
     * @param type 记忆类型
     * @param limit 最大返回数量
     * @return 记忆列表
     */
    List<LongTermMemory> getMemoriesByType(String userId, LongTermMemory.MemoryType type, int limit);
    
    /**
     * 语义搜索记忆
     * @param userId 用户 ID
     * @param query 查询文本
     * @param maxResults 最大返回结果数
     * @param minScore 最小相似度分数
     * @return 匹配的记忆列表
     */
    List<LongTermMemory> searchMemories(String userId, String query, int maxResults, double minScore);
    
    /**
     * 更新记忆
     * @param memory 记忆对象（必须包含 ID）
     * @return 更新后的记忆
     */
    LongTermMemory updateMemory(LongTermMemory memory);
    
    /**
     * 删除记忆
     * @param id 记忆 ID
     * @return 是否删除成功
     */
    boolean deleteMemory(String id);
    
    /**
     * 批量删除记忆
     * @param ids 记忆 ID 列表
     * @return 删除的数量
     */
    int deleteMemories(List<String> ids);
    
    /**
     * 清理过期记忆
     * @return 清理的记忆数量
     */
    int cleanupExpiredMemories();
    
    /**
     * 压缩记忆（将多个相关记忆合并为摘要）
     * @param userId 用户 ID
     * @param memoryIds 要压缩的记忆 ID 列表
     * @param summary 摘要内容
     * @return 新生成的摘要记忆
     */
    LongTermMemory compressMemories(String userId, List<String> memoryIds, String summary);
    
    /**
     * 更新记忆重要性评分
     * @param id 记忆 ID
     * @param newScore 新的评分
     * @return 更新后的记忆
     */
    LongTermMemory updateImportanceScore(String id, Double newScore);
}
