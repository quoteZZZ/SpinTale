package com.spintale.ai.agent.memory.impl;

import com.alibaba.fastjson2.JSON;
import com.spintale.ai.agent.memory.LongTermMemory;
import com.spintale.ai.agent.memory.api.MemoryStore;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Redis 持久化记忆存储
 *
 * 替代原 InMemoryLongTermMemoryManager 中的 ConcurrentHashMap 存储
 * 使用 Redisson 客户端操作 Redis，支持：
 * - 持久化存储，重启不丢失
 * - 分布式共享（多实例部署）
 * - TTL 自动过期
 * - 高效的按用户索引查询
 *
 * 数据结构：
 * - spintale:memory:item:{id}    -> Hash (记忆详情)
 * - spintale:memory:user:{uid}   -> Set (用户记忆 ID 索引)
 * - spintale:memory:type:{uid}:{type} -> Set (用户类型索引)
 */
public class RedisMemoryStore implements MemoryStore {

    private static final Logger log = LoggerFactory.getLogger(RedisMemoryStore.class);

    private static final String ITEM_PREFIX = "spintale:memory:item:";
    private static final String USER_INDEX_PREFIX = "spintale:memory:user:";
    private static final String TYPE_INDEX_PREFIX = "spintale:memory:type:";
    private static final long DEFAULT_TTL_HOURS = 720; // 30 天

    private final RedissonClient redissonClient;

    public RedisMemoryStore(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Override
    public LongTermMemory save(LongTermMemory memory) {
        if (memory.getId() == null) {
            memory.setId(UUID.randomUUID().toString());
        }
        if (memory.getCreatedAt() == null) {
            memory.setCreatedAt(LocalDateTime.now());
        }
        if (memory.getAccessCount() == null) {
            memory.setAccessCount(0);
        }

        String itemKey = ITEM_PREFIX + memory.getId();

        try {
            // 保存记忆详情
            RMap<String, String> itemMap = redissonClient.getMap(itemKey);
            Map<String, String> fields = memoryToMap(memory);
            itemMap.putAll(fields);
            itemMap.expire(DEFAULT_TTL_HOURS, TimeUnit.HOURS);

            // 更新用户索引
            String userKey = USER_INDEX_PREFIX + memory.getUserId();
            redissonClient.getSet(userKey).add(memory.getId());

            // 更新类型索引
            if (memory.getType() != null) {
                String typeKey = TYPE_INDEX_PREFIX + memory.getUserId() + ":" + memory.getType().name();
                redissonClient.getSet(typeKey).add(memory.getId());
            }

            log.debug("Saved memory to Redis: id={}, userId={}", memory.getId(), memory.getUserId());
        } catch (Exception e) {
            log.error("Failed to save memory to Redis: {}", e.getMessage(), e);
        }

        return memory;
    }

    @Override
    public Optional<LongTermMemory> findById(String id) {
        String itemKey = ITEM_PREFIX + id;
        try {
            RMap<String, String> itemMap = redissonClient.getMap(itemKey);
            if (itemMap.isEmpty()) {
                return Optional.empty();
            }

            LongTermMemory memory = mapToMemory(itemMap);

            // 更新访问计数
            itemMap.put("accessCount", String.valueOf(memory.getAccessCount() + 1));
            itemMap.put("lastAccessedAt", LocalDateTime.now().toString());

            return Optional.of(memory);
        } catch (Exception e) {
            log.error("Failed to find memory by id: {}", id, e);
            return Optional.empty();
        }
    }

    @Override
    public List<LongTermMemory> findByUserId(String userId, int limit) {
        String userKey = USER_INDEX_PREFIX + userId;
        try {
            Set<String> memoryIds = redissonClient.<String>getSet(userKey).readAll();

            return memoryIds.stream()
                    .map(this::findById)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .filter(m -> m.getExpiresAt() == null || m.getExpiresAt().isAfter(LocalDateTime.now()))
                    .sorted((a, b) -> {
                        int cmp = Double.compare(
                                b.getImportanceScore() != null ? b.getImportanceScore() : 0,
                                a.getImportanceScore() != null ? a.getImportanceScore() : 0);
                        if (cmp != 0) return cmp;
                        return b.getCreatedAt().compareTo(a.getCreatedAt());
                    })
                    .limit(limit)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to find memories by userId: {}", userId, e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<LongTermMemory> findByUserIdAndType(String userId, LongTermMemory.MemoryType type, int limit) {
        String typeKey = TYPE_INDEX_PREFIX + userId + ":" + type.name();
        try {
            Set<String> memoryIds = redissonClient.<String>getSet(typeKey).readAll();

            return memoryIds.stream()
                    .map(this::findById)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .limit(limit)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to find memories by type: userId={}, type={}", userId, type, e);
            return Collections.emptyList();
        }
    }

    @Override
    public LongTermMemory update(LongTermMemory memory) {
        if (memory.getId() == null) {
            throw new IllegalArgumentException("Memory id cannot be null for update");
        }

        memory.setLastAccessedAt(LocalDateTime.now());
        return save(memory);
    }

    @Override
    public boolean deleteById(String id) {
        String itemKey = ITEM_PREFIX + id;
        try {
            // 先获取记忆详情以更新索引
            RMap<String, String> itemMap = redissonClient.getMap(itemKey);
            if (itemMap.isEmpty()) {
                return false;
            }

            String userId = itemMap.get("userId");
            String typeName = itemMap.get("type");

            // 删除记忆详情
            itemMap.delete();

            // 更新用户索引
            if (userId != null) {
                redissonClient.getSet(USER_INDEX_PREFIX + userId).remove(id);
            }

            // 更新类型索引
            if (userId != null && typeName != null) {
                redissonClient.getSet(TYPE_INDEX_PREFIX + userId + ":" + typeName).remove(id);
            }

            log.debug("Deleted memory from Redis: id={}", id);
            return true;
        } catch (Exception e) {
            log.error("Failed to delete memory: id={}", id, e);
            return false;
        }
    }

    @Override
    public int deleteByIds(List<String> ids) {
        int count = 0;
        for (String id : ids) {
            if (deleteById(id)) {
                count++;
            }
        }
        return count;
    }

    @Override
    public int cleanupExpired(String userId) {
        List<LongTermMemory> memories = findByUserId(userId, Integer.MAX_VALUE);
        int count = 0;
        LocalDateTime now = LocalDateTime.now();

        for (LongTermMemory memory : memories) {
            if (memory.getExpiresAt() != null && memory.getExpiresAt().isBefore(now)) {
                if (deleteById(memory.getId())) {
                    count++;
                }
            }
        }

        log.info("Cleaned up {} expired memories for user {}", count, userId);
        return count;
    }

    // ==================== 序列化工具方法 ====================

    private static final java.time.format.DateTimeFormatter DATE_TIME_FORMATTER = 
        java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private Map<String, String> memoryToMap(LongTermMemory memory) {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("id", memory.getId());
        map.put("userId", memory.getUserId());
        map.put("content", memory.getContent());
        map.put("type", memory.getType() != null ? memory.getType().name() : LongTermMemory.MemoryType.FACT.name());
        map.put("importanceScore", memory.getImportanceScore() != null ? memory.getImportanceScore().toString() : "0.5");
        map.put("accessCount", memory.getAccessCount() != null ? memory.getAccessCount().toString() : "0");
        map.put("createdAt", memory.getCreatedAt() != null ? memory.getCreatedAt().format(DATE_TIME_FORMATTER) : "");
        map.put("lastAccessedAt", memory.getLastAccessedAt() != null ? memory.getLastAccessedAt().format(DATE_TIME_FORMATTER) : "");
        map.put("expiresAt", memory.getExpiresAt() != null ? memory.getExpiresAt().format(DATE_TIME_FORMATTER) : "");
        if (memory.getMetadata() != null) {
            map.put("metadata", JSON.toJSONString(memory.getMetadata()));
        }
        return map;
    }

    private LongTermMemory mapToMemory(RMap<String, String> map) {
        LongTermMemory memory = new LongTermMemory();
        memory.setId(map.get("id"));
        memory.setUserId(map.get("userId"));
        memory.setContent(map.get("content"));
        memory.setType(map.get("type") != null ? LongTermMemory.MemoryType.valueOf(map.get("type")) : LongTermMemory.MemoryType.FACT);
        memory.setImportanceScore(map.get("importanceScore") != null ? Double.parseDouble(map.get("importanceScore")) : 0.5);
        memory.setAccessCount(map.get("accessCount") != null ? Integer.parseInt(map.get("accessCount")) : 0);
        memory.setCreatedAt(parseDateTime(map.get("createdAt")));
        memory.setLastAccessedAt(parseDateTime(map.get("lastAccessedAt")));
        memory.setExpiresAt(parseDateTime(map.get("expiresAt")));
        if (map.get("metadata") != null) {
            memory.setMetadata(JSON.parseObject(map.get("metadata"), Map.class));
        }
        return memory;
    }

    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateTimeStr, DATE_TIME_FORMATTER);
        } catch (Exception e) {
            log.warn("Failed to parse date time: {}", dateTimeStr, e);
            return null;
        }
    }
}
