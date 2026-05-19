package com.spintale.ai.capability.memory;

import com.spintale.ai.capability.memory.LongTermMemory;

import java.util.List;
import java.util.Optional;

/**
 * 记忆存储接口
 * 支持多种后端实现：JDBC、Redis、向量数据库
 */
public interface MemoryStore {

    /**
     * 保存记忆
     */
    LongTermMemory save(LongTermMemory memory);

    /**
     * 根据 ID 获取记忆
     */
    Optional<LongTermMemory> findById(String id);

    /**
     * 根据用户 ID 查询记忆
     */
    List<LongTermMemory> findByUserId(String userId, int limit);

    /**
     * 根据类型查询记忆
     */
    List<LongTermMemory> findByUserIdAndType(String userId, LongTermMemory.MemoryType type, int limit);

    /**
     * 更新记忆
     */
    LongTermMemory update(LongTermMemory memory);

    /**
     * 删除记忆
     */
    boolean deleteById(String id);

    /**
     * 批量删除
     */
    int deleteByIds(List<String> ids);

    /**
     * 清理过期记忆
     */
    int cleanupExpired(String userId);
}
