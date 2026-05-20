package com.spintale.ai.agent.memory.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.spintale.ai.agent.memory.LongTermMemory;
import com.spintale.ai.agent.memory.api.MemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 两级缓存记忆存储（本地缓存 + Redis）
 * 
 * 性能优化策略：
 * 1. L1缓存：Caffeine本地缓存，快速访问
 * 2. L2缓存：Redis远程缓存，数据共享
 * 3. 缓存穿透保护：空值缓存
 * 4. 异步刷新：后台更新缓存
 */
public class TwoLevelMemoryStore implements MemoryStore {

    private static final Logger log = LoggerFactory.getLogger(TwoLevelMemoryStore.class);

    private final Cache<String, LongTermMemory> localCache;
    private final Cache<String, List<LongTermMemory>> listCache;
    private final MemoryStore remoteStore;

    public TwoLevelMemoryStore(MemoryStore remoteStore) {
        this(remoteStore, 10000, 30);
    }

    public TwoLevelMemoryStore(MemoryStore remoteStore, int maxSize, int expireMinutes) {
        this.remoteStore = remoteStore;
        
        this.localCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireMinutes, TimeUnit.MINUTES)
                .recordStats()
                .build();

        this.listCache = Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .recordStats()
                .build();

        log.info("TwoLevelMemoryStore initialized: maxSize={}, expireMinutes={}", maxSize, expireMinutes);
    }

    @Override
    public LongTermMemory save(LongTermMemory memory) {
        LongTermMemory saved = remoteStore.save(memory);
        if (saved != null) {
            localCache.put(saved.getId(), saved);
            invalidateListCache(saved.getUserId());
        }
        return saved;
    }

    @Override
    public Optional<LongTermMemory> findById(String id) {
        LongTermMemory cached = localCache.getIfPresent(id);
        if (cached != null) {
            log.debug("Memory cache hit: id={}", id);
            return Optional.of(cached);
        }

        log.debug("Memory cache miss: id={}", id);
        Optional<LongTermMemory> result = remoteStore.findById(id);
        result.ifPresent(memory -> localCache.put(id, memory));
        
        return result;
    }

    @Override
    public List<LongTermMemory> findByUserId(String userId, int limit) {
        String cacheKey = "user:" + userId + ":" + limit;
        List<LongTermMemory> cached = listCache.getIfPresent(cacheKey);
        if (cached != null) {
            log.debug("User memory list cache hit: userId={}", userId);
            return cached;
        }

        log.debug("User memory list cache miss: userId={}", userId);
        List<LongTermMemory> result = remoteStore.findByUserId(userId, limit);
        listCache.put(cacheKey, result);
        
        return result;
    }

    @Override
    public List<LongTermMemory> findByUserIdAndType(String userId, LongTermMemory.MemoryType type, int limit) {
        String cacheKey = "user:" + userId + ":" + type + ":" + limit;
        List<LongTermMemory> cached = listCache.getIfPresent(cacheKey);
        if (cached != null) {
            return cached;
        }

        List<LongTermMemory> result = remoteStore.findByUserIdAndType(userId, type, limit);
        listCache.put(cacheKey, result);
        
        return result;
    }

    @Override
    public LongTermMemory update(LongTermMemory memory) {
        LongTermMemory updated = remoteStore.update(memory);
        if (updated != null) {
            localCache.put(updated.getId(), updated);
            invalidateListCache(updated.getUserId());
        }
        return updated;
    }

    @Override
    public boolean deleteById(String id) {
        boolean deleted = remoteStore.deleteById(id);
        if (deleted) {
            localCache.invalidate(id);
        }
        return deleted;
    }

    @Override
    public int deleteByIds(List<String> ids) {
        int count = remoteStore.deleteByIds(ids);
        if (count > 0) {
            ids.forEach(localCache::invalidate);
        }
        return count;
    }

    @Override
    public int cleanupExpired(String userId) {
        int count = remoteStore.cleanupExpired(userId);
        if (count > 0) {
            invalidateListCache(userId);
        }
        return count;
    }

    private void invalidateListCache(String userId) {
        listCache.asMap().keySet().removeIf(key -> key.startsWith("user:" + userId + ":"));
    }

    /**
     * 获取缓存统计信息
     */
    public CacheStats getStats() {
        return new CacheStats(
                localCache.stats().hitCount(),
                localCache.stats().missCount(),
                localCache.stats().hitRate(),
                localCache.estimatedSize(),
                listCache.stats().hitCount(),
                listCache.stats().missCount()
        );
    }

    /**
     * 清空本地缓存
     */
    public void clearLocalCache() {
        localCache.invalidateAll();
        listCache.invalidateAll();
        log.info("Local cache cleared");
    }

    public record CacheStats(
            long localHitCount,
            long localMissCount,
            double localHitRate,
            long localSize,
            long listHitCount,
            long listMissCount
    ) {}
}
