package com.spintale.ai.infrastructure.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.concurrent.TimeUnit;

/**
 * Caffeine 本地缓存配置
 * 提供高性能的本地缓存层，与 Redis 形成二级缓存架构
 */
@Configuration
public class CaffeineCacheConfig {

    /**
     * 配置 Caffeine 缓存管理器
     * 
     * @return CacheManager
     */
    @Bean
    @Primary
    public CacheManager caffeineCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        
        // 注册自定义缓存
        cacheManager.registerCustomCache("memory-cache", 
            Caffeine.newBuilder()
                .maximumSize(10000)           // 最大 10000 条记录
                .expireAfterWrite(10, TimeUnit.MINUTES)  // 写入后 10 分钟过期
                .expireAfterAccess(5, TimeUnit.MINUTES)  // 访问后 5 分钟过期
                .recordStats()                // 开启统计信息
                .build());
        
        cacheManager.registerCustomCache("rag-cache",
            Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .recordStats()
                .build());
        
        cacheManager.registerCustomCache("llm-response-cache",
            Caffeine.newBuilder()
                .maximumSize(5000)
                .expireAfterWrite(1, TimeUnit.HOURS)
                .recordStats()
                .build());
        
        return cacheManager;
    }
}
