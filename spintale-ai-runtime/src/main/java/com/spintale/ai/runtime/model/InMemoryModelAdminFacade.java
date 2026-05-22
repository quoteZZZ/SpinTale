package com.spintale.ai.runtime.model;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import com.spintale.ai.provider.catalog.ModelCatalog;
import com.spintale.ai.provider.catalog.ModelCapability;
import com.spintale.ai.provider.catalog.ProviderCatalog;

@Component
public class InMemoryModelAdminFacade implements ModelAdminFacade
{
    private final Map<String, ModelCatalog> models = new ConcurrentHashMap<>();
    private final Map<String, ProviderCatalog> providers = new ConcurrentHashMap<>();

    @Override
    public List<ModelCatalog> listAllModels()
    {
        return new ArrayList<>(models.values());
    }

    @Override
    public List<ModelCatalog> listModelsByProvider(String providerId)
    {
        return models.values().stream()
                .filter(m -> providerId.equals(m.getProvider()))
                .collect(Collectors.toList());
    }

    @Override
    public List<ModelCatalog> listModelsByType(String modelType)
    {
        return models.values().stream()
                .filter(m -> modelType.equalsIgnoreCase(m.getModelType()))
                .collect(Collectors.toList());
    }

    @Override
    public List<ModelCatalog> listModelsByCapability(ModelCapability.Capability capability)
    {
        return models.values().stream()
                .filter(m -> m.getCapability() != null && m.getCapability().hasCapability(capability))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ModelCatalog> getModel(String modelId)
    {
        return Optional.ofNullable(models.get(modelId));
    }

    @Override
    public ModelCatalog registerModel(ModelCatalog model)
    {
        models.put(model.getModelId(), model);
        return model;
    }

    @Override
    public void unregisterModel(String modelId)
    {
        models.remove(modelId);
    }

    @Override
    public void enableModel(String modelId)
    {
        ModelCatalog model = models.get(modelId);
        if (model != null)
        {
            model.setEnabled(true);
        }
    }

    @Override
    public void disableModel(String modelId)
    {
        ModelCatalog model = models.get(modelId);
        if (model != null)
        {
            model.setEnabled(false);
        }
    }

    @Override
    public List<ProviderCatalog> listAllProviders()
    {
        return new ArrayList<>(providers.values());
    }

    @Override
    public Optional<ProviderCatalog> getProvider(String providerId)
    {
        return Optional.ofNullable(providers.get(providerId));
    }

    @Override
    public ProviderCatalog registerProvider(ProviderCatalog provider)
    {
        providers.put(provider.getProviderId(), provider);
        return provider;
    }

    @Override
    public void updateProviderHealth(String providerId, ProviderCatalog.HealthStatus status)
    {
        ProviderCatalog provider = providers.get(providerId);
        if (provider != null)
        {
            provider.updateHealth(status);
        }
    }

    @Override
    public List<ModelCatalog> getAvailableModels()
    {
        return models.values().stream()
                .filter(ModelCatalog::isEnabled)
                .filter(m -> {
                    ProviderCatalog provider = providers.get(m.getProvider());
                    return provider != null && provider.isAvailable();
                })
                .collect(Collectors.toList());
    }

    @Override
    public boolean isModelAvailable(String modelId)
    {
        ModelCatalog model = models.get(modelId);
        if (model == null || !model.isEnabled())
        {
            return false;
        }
        ProviderCatalog provider = providers.get(model.getProvider());
        return provider != null && provider.isAvailable();
    }

    @Override
    public boolean isProviderHealthy(String providerId)
    {
        ProviderCatalog provider = providers.get(providerId);
        return provider != null && provider.isHealthy();
    }

    @Override
    public ModelCostEstimate estimateCost(String modelId, int inputTokens, int outputTokens)
    {
        ModelCatalog model = models.get(modelId);
        if (model == null)
        {
            return new ModelCostEstimate(modelId, inputTokens, outputTokens, 0.0, "USD");
        }

        double cost = model.estimateCost(inputTokens, outputTokens);
        String currency = model.getPricing() != null ? 
                model.getPricing().getCurrency() : "USD";

        return new ModelCostEstimate(modelId, inputTokens, outputTokens, cost, currency);
    }

    @Override
    public Set<ModelCapability.Capability> getModelCapabilities(String modelId)
    {
        ModelCatalog model = models.get(modelId);
        if (model == null || model.getCapability() == null)
        {
            return Collections.emptySet();
        }
        return model.getCapability().getCapabilities();
    }

    @Override
    public boolean hasCapability(String modelId, ModelCapability.Capability capability)
    {
        ModelCatalog model = models.get(modelId);
        if (model == null || model.getCapability() == null)
        {
            return false;
        }
        return model.getCapability().hasCapability(capability);
    }

    public void clear()
    {
        models.clear();
        providers.clear();
    }
}
