package com.spintale.ai.runtime.model;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import com.spintale.ai.provider.catalog.ModelCatalog;
import com.spintale.ai.provider.catalog.ModelCapability;
import com.spintale.ai.provider.catalog.ProviderCatalog;

public interface ModelAdminFacade
{
    List<ModelCatalog> listAllModels();

    List<ModelCatalog> listModelsByProvider(String providerId);

    List<ModelCatalog> listModelsByType(String modelType);

    List<ModelCatalog> listModelsByCapability(ModelCapability.Capability capability);

    Optional<ModelCatalog> getModel(String modelId);

    ModelCatalog registerModel(ModelCatalog model);

    void unregisterModel(String modelId);

    void enableModel(String modelId);

    void disableModel(String modelId);

    List<ProviderCatalog> listAllProviders();

    Optional<ProviderCatalog> getProvider(String providerId);

    ProviderCatalog registerProvider(ProviderCatalog provider);

    void updateProviderHealth(String providerId, ProviderCatalog.HealthStatus status);

    List<ModelCatalog> getAvailableModels();

    boolean isModelAvailable(String modelId);

    boolean isProviderHealthy(String providerId);

    ModelCostEstimate estimateCost(String modelId, int inputTokens, int outputTokens);

    Set<ModelCapability.Capability> getModelCapabilities(String modelId);

    boolean hasCapability(String modelId, ModelCapability.Capability capability);

    record ModelCostEstimate(
            String modelId,
            int inputTokens,
            int outputTokens,
            double estimatedCost,
            String currency
    ) {}
}
