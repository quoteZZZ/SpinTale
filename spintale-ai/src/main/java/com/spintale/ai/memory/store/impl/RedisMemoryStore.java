package com.spintale.ai.memory.store.impl;

import com.spintale.ai.memory.LongTermMemory;
import com.spintale.ai.memory.store.MemoryStore;
import org.redisson.api.RedissonClient;
import org.redisson.api.RMap;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Redis 记忆存储实现
 * 使用 Redis 作为长期记忆的缓存层
 * 支持 TTL 过期和批量操作
 */
public class RedisMemoryStore implements MemoryStore {

    private static final String MEMORY_KEY_PREFIX = "spintale:memory:";
    private static final String USER_MEMORY_INDEX_KEY = "spintale:memory:user:index:";
    
    private final RedissonClient redissonClient;
    private final long defaultTtlHours;

    public RedisMemoryStore(RedissonClient redissonClient) {
        this(redissonClient, 24); // 默认 24 小时过期
    }

    public RedisMemoryStore(RedissonClient redissonClient, long defaultTtlHours) {
        this.redissonClient = redissonClient;
        this.defaultTtlHours = defaultTtlHours;
    }

    @Override
    public void save(LongTermMemory memory) {
        String memoryKey = MEMORY_KEY_PREFIX + memory.getId();
        RMap<String, Object> memoryMap = redissonClient.getMap(memoryKey);
        
        memoryMap.put("id", memory.getId());
        memoryMap.put("userId", memory.getUserId());
        memoryMap.put("type", memory.getType().name());
        memoryMap.put("content", memory.getContent());
        memoryMap.put("summary", memory.getSummary());
        memoryMap.put("keywords", memory.getKeywords());
        memoryMap.put("importance", memory.getImportance());
        memoryMap.put("createdAt", memory.getCreatedAt());
        memoryMap.put("updatedAt", LocalDateTime.now());
        
        // 设置过期时间
        redissonClient.getMap(memoryKey).expire(defaultTtlHours, TimeUnit.HOURS);
        
        // 更新用户索引
        updateUserIndex(memory.getUserId(), memory.getId());
    }

    @Override
    public LongTermMemory findById(String id) {
        String memoryKey = MEMORY_KEY_PREFIX + id;
        RMap<String, Object> memoryMap = redissonClient.getMap(memoryKey);
        
        if (memoryMap.isEmpty()) {
            return null;
        }
        
        return buildMemoryFromMap(memoryMap);
    }

    @Override
    public List<LongTermMemory> findByUserId(String userId, int limit) {
        String indexKey = USER_MEMORY_INDEX_KEY + userId;
        RMap<String, Long> indexMap = redissonClient.getMap(indexKey);
        
        Set<String> memoryIds = indexMap.keySet();
        List<LongTermMemory> memories = new ArrayList<>();
        
        int count = 0;
        for (String memoryId : memoryIds) {
            if (count >= limit) {
                break;
            }
            LongTermMemory memory = findById(memoryId);
            if (memory != null) {
                memories.add(memory);
                count++;
            }
        }
        
        return memories;
    }

    @Override
    public List<LongTermMemory> findByType(String userId, LongTermMemory.MemoryType type, int limit) {
        List<LongTermMemory> allMemories = findByUserId(userId, limit * 2);
        
        return allMemories.stream()
            .filter(m -> m.getType() == type)
            .limit(limit)
            .collect(Collectors.toList());
    }

    @Override
    public void update(LongTermMemory memory) {
        memory.setUpdatedAt(LocalDateTime.now());
        save(memory);
    }

    @Override
    public boolean delete(String id) {
        String memoryKey = MEMORY_KEY_PREFIX + id;
        RMap<String, Object> memoryMap = redissonClient.getMap(memoryKey);
        
        if (!memoryMap.isEmpty()) {
            LongTermMemory memory = buildMemoryFromMap(memoryMap);
            if (memory != null) {
                removeFromUserIndex(memory.getUserId(), id);
            }
        }
        
        return redissonClient.getMap(memoryKey).delete() > 0;
    }

    @Override
    public int deleteBatch(List<String> ids) {
        int deletedCount = 0;
        for (String id : ids) {
            if (delete(id)) {
                deletedCount++;
            }
        }
        return deletedCount;
    }

    @Override
    public List<String> getAllMemoryIds() {
        return new ArrayList<>();
    }

    private void updateUserIndex(String userId, String memoryId) {
        String indexKey = USER_MEMORY_INDEX_KEY + userId;
        RMap<String, Long> indexMap = redissonClient.getMap(indexKey);
        indexMap.put(memoryId, System.currentTimeMillis());
        indexMap.expire(defaultTtlHours, TimeUnit.HOURS);
    }

    private void removeFromUserIndex(String userId, String memoryId) {
        String indexKey = USER_MEMORY_INDEX_KEY + userId;
        RMap<String, Long> indexMap = redissonClient.getMap(indexKey);
        indexMap.remove(memoryId);
    }

    @SuppressWarnings("unchecked")
    private LongTermMemory buildMemoryFromMap(RMap<String, Object> memoryMap) {
        LongTermMemory memory = new LongTermMemory();
        memory.setId((String) memoryMap.get("id"));
        memory.setUserId((String) memoryMap.get("userId"));
        memory.setType(LongTermMemory.MemoryType.valueOf((String) memoryMap.get("type")));
        memory.setContent((String) memoryMap.get("content"));
        memory.setSummary((String) memoryMap.get("summary"));
        memory.setKeywords((List<String>) memoryMap.get("keywords"));
        memory.setImportance((Integer) memoryMap.get("importance"));
        memory.setCreatedAt((LocalDateTime) memoryMap.get("createdAt"));
        memory.setUpdatedAt((LocalDateTime) memoryMap.get("updatedAt"));
        return memory;
    }
}
