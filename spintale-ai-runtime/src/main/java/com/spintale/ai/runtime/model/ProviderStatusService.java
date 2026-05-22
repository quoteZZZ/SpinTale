package com.spintale.ai.runtime.model;

import java.util.List;
import java.util.Optional;
import com.spintale.ai.provider.catalog.ModelCatalog;
import com.spintale.ai.provider.catalog.ProviderCatalog;

public interface ProviderStatusService
{
    List<ProviderStatus> getAllProviderStatuses();

    Optional<ProviderStatus> getProviderStatus(String providerId);

    ProviderStatus checkHealth(String providerId);

    void startHealthMonitoring();

    void stopHealthMonitoring();

    void setHealthCheckInterval(String providerId, long intervalSeconds);

    record ProviderStatus(
            String providerId,
            String providerName,
            ProviderCatalog.ProviderType type,
            ProviderCatalog.HealthStatus healthStatus,
            long latencyMs,
            int availableModels,
            int totalModels,
            long lastCheckTime,
            String errorMessage
    ) {
        public boolean isHealthy()
        {
            return healthStatus == ProviderCatalog.HealthStatus.HEALTHY;
        }

        public boolean isAvailable()
        {
            return healthStatus != ProviderCatalog.HealthStatus.UNHEALTHY;
        }
    }
}
