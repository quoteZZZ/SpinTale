package com.spintale.ai.memory.store;

import com.spintale.ai.memory.LongTermMemory;
import java.util.List;

/**
 * 记忆存储接口
 * 支持多种后端实现：JDBC、Redis、向量数据库
 */
public interface MemoryStore {
    
    /**
     * 保存记忆
     */
    void save(LongTermMemory memory);
    
    /**
     * 根据 ID 获取记忆
     */
    LongTermMemory findById(String id);
    
    /**
     * 根据用户 ID 查询记忆
     */
    List<LongTermMemory> findByUserId(String userId, int limit);
    
    /**
     * 根据类型查询记忆
     */
    List<LongTermMemory> findByType(String userId, LongTermMemory.MemoryType type, int limit);
    
    /**
     * 更新记忆
     */
    void update(LongTermMemory memory);
    
    /**
     * 删除记忆
     */
    boolean delete(String id);
    
    /**
     * 批量删除
     */
    int deleteBatch(List<String> ids);
    
    /**
     * 获取所有记忆 ID
     */
    List<String> getAllMemoryIds();
}
