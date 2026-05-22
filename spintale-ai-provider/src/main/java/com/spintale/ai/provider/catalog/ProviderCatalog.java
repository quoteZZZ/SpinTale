package com.spintale.ai.provider.catalog;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProviderCatalog
{
    private String providerId;
    private String providerName;
    private ProviderType providerType;
    private String baseUrl;
    private String apiKeyRef;
    private boolean enabled;
    private HealthStatus healthStatus;
    private Instant lastHealthCheck;
    private Map<String, Object> config;
    private Duration healthCheckInterval;

    public enum ProviderType
    {
        OPENAI,
        OPENAI_COMPATIBLE,
        AZURE_OPENAI,
        OLLAMA,
        ANTHROPIC,
        GOOGLE,
        LOCAL,
        CUSTOM
    }

    public enum HealthStatus
    {
        HEALTHY,
        DEGRADED,
        UNHEALTHY,
        UNKNOWN
    }

    public boolean isHealthy()
    {
        return healthStatus == HealthStatus.HEALTHY;
    }

    public boolean isAvailable()
    {
        return enabled && healthStatus != HealthStatus.UNHEALTHY;
    }

    public void updateHealth(HealthStatus status)
    {
        this.healthStatus = status;
        this.lastHealthCheck = Instant.now();
    }

    public static ProviderCatalog of(String providerId, String name, ProviderType type)
    {
        return ProviderCatalog.builder()
                .providerId(providerId)
                .providerName(name)
                .providerType(type)
                .enabled(true)
                .healthStatus(HealthStatus.UNKNOWN)
                .healthCheckInterval(Duration.ofMinutes(5))
                .build();
    }

    public static ProviderCatalog openai(String apiKeyRef)
    {
        return ProviderCatalog.builder()
                .providerId("openai")
                .providerName("OpenAI")
                .providerType(ProviderType.OPENAI)
                .baseUrl("https://api.openai.com/v1")
                .apiKeyRef(apiKeyRef)
                .enabled(true)
                .healthStatus(HealthStatus.UNKNOWN)
                .healthCheckInterval(Duration.ofMinutes(5))
                .build();
    }

    public static ProviderCatalog ollama(String baseUrl)
    {
        return ProviderCatalog.builder()
                .providerId("ollama")
                .providerName("Ollama")
                .providerType(ProviderType.OLLAMA)
                .baseUrl(baseUrl)
                .enabled(true)
                .healthStatus(HealthStatus.UNKNOWN)
                .healthCheckInterval(Duration.ofMinutes(1))
                .build();
    }
}
