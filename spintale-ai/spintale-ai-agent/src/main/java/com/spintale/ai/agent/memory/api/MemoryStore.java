package com.spintale.ai.agent.memory.api;

import com.spintale.ai.agent.memory.LongTermMemory;

import java.util.List;
import java.util.Optional;

/**
 * 记忆存储接口
 * 支持多种后端实现：JDBC、Redis、向量数据库
 */
public interface MemoryStore {

    /**
     * 保存记忆
     *
     * @param memory 记忆对象
     * @return 保存后的记忆
     */
    LongTermMemory save(LongTermMemory memory);

    /**
     * 根据ID获取记忆
     *
     * @param id 记忆ID
     * @return 记忆对象（可选）
     */
    Optional<LongTermMemory> findById(String id);

    /**
     * 根据用户ID查询记忆
     *
     * @param userId 用户ID
     * @param limit 限制数量
     * @return 记忆列表
     */
    List<LongTermMemory> findByUserId(String userId, int limit);

    /**
     * 根据类型查询记忆
     *
     * @param userId 用户ID
     * @param type 记忆类型
     * @param limit 限制数量
     * @return 记忆列表
     */
    List<LongTermMemory> findByUserIdAndType(String userId, LongTermMemory.MemoryType type, int limit);

    /**
     * 更新记忆
     *
     * @param memory 记忆对象
     * @return 更新后的记忆
     */
    LongTermMemory update(LongTermMemory memory);

    /**
     * 删除记忆
     *
     * @param id 记忆ID
     * @return 是否成功
     */
    boolean deleteById(String id);

    /**
     * 批量删除
     *
     * @param ids ID列表
     * @return 删除数量
     */
    int deleteByIds(List<String> ids);

    /**
     * 清理过期记忆
     *
     * @param userId 用户ID
     * @return 清理数量
     */
    int cleanupExpired(String userId);

    /**
     * 获取存储类型
     *
     * @return 存储类型名称
     */
    String getStoreType();
}
