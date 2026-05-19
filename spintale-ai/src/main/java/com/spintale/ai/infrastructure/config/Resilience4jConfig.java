package com.spintale.ai.infrastructure.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;

/**
 * Resilience4j 容错配置
 * 为 LLM 调用、RAG 检索等提供熔断、重试、限流保护
 */
@Configuration
public class Resilience4jConfig {

    /**
     * 配置 LLM 调用的熔断器
     * 当 OpenAI/Ollama 服务不可用时快速失败，避免资源耗尽
     */
    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        CircuitBreakerConfig llmCircuitBreakerConfig = CircuitBreakerConfig.custom()
            .failureRateThreshold(50)              // 失败率超过 50% 触发熔断
            .waitDurationInOpenState(Duration.ofSeconds(30))  // 熔断后等待 30 秒
            .slidingWindowSize(10)                 // 滑动窗口大小 10
            .minimumNumberOfCalls(5)               // 最少 5 次调用后才计算失败率
            .permittedNumberOfCallsInHalfOpenState(3)  // 半开状态允许 3 次调用
            .automaticTransitionFromOpenToHalfOpenEnabled(true)  // 自动从打开状态转为半开
            .build();

        CircuitBreakerConfig ragCircuitBreakerConfig = CircuitBreakerConfig.custom()
            .failureRateThreshold(60)              // RAG 失败率阈值稍高
            .waitDurationInOpenState(Duration.ofSeconds(10))
            .slidingWindowSize(5)
            .minimumNumberOfCalls(3)
            .build();

        return CircuitBreakerRegistry.of(Map.of(
            "llmService", llmCircuitBreakerConfig,
            "ragService", ragCircuitBreakerConfig
        ));
    }

    /**
     * 配置重试机制
     * 对网络波动导致的临时失败进行自动重试
     */
    @Bean
    public RetryRegistry retryRegistry() {
        RetryConfig llmRetryConfig = RetryConfig.custom()
            .maxAttempts(3)                       // 最多重试 3 次
            .waitDuration(Duration.ofMillis(500))  // 每次重试间隔 500ms
            .retryExceptions(java.net.ConnectException.class, 
                             java.util.concurrent.TimeoutException.class)
            .ignoreExceptions(io.github.resilience4j.circuitbreaker.CallNotPermittedException.class)  // 熔断时不重试
            .build();

        RetryConfig ragRetryConfig = RetryConfig.custom()
            .maxAttempts(2)
            .waitDuration(Duration.ofMillis(200))
            .build();

        return RetryRegistry.of(Map.of(
            "llmService", llmRetryConfig,
            "ragService", ragRetryConfig
        ));
    }

    /**
     * 配置超时限制
     * 防止 LLM 调用或 RAG 检索长时间阻塞
     */
    @Bean
    public TimeLimiterRegistry timeLimiterRegistry() {
        TimeLimiterConfig llmTimeLimiterConfig = TimeLimiterConfig.custom()
            .timeoutDuration(Duration.ofSeconds(30))  // LLM 调用超时 30 秒
            .cancelRunningFuture(true)                // 超时时取消未来任务
            .build();

        TimeLimiterConfig ragTimeLimiterConfig = TimeLimiterConfig.custom()
            .timeoutDuration(Duration.ofSeconds(10))  // RAG 检索超时 10 秒
            .build();

        return TimeLimiterRegistry.of(Map.of(
            "llmService", llmTimeLimiterConfig,
            "ragService", ragTimeLimiterConfig
        ));
    }
}
