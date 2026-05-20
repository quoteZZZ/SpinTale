package com.spintale.ai.core.spi;

import com.spintale.ai.core.exception.AiServiceException;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Registry for managing model providers with routing capabilities.
 */
@Slf4j
public class ModelProviderRegistry {

    private final Map<String, ModelProvider> providers = new ConcurrentHashMap<>();
    private volatile String defaultProviderId;

    /**
     * Register a model provider.
     *
     * @param provider the provider to register
     */
    public void register(ModelProvider provider) {
        if (provider == null) {
            throw new IllegalArgumentException("Provider cannot be null");
        }
        
        String id = provider.getId();
        if (providers.containsKey(id)) {
            log.warn("Provider {} already registered, replacing existing instance", id);
        }
        
        providers.put(id, provider);
        
        // Set first registered provider as default
        if (defaultProviderId == null) {
            defaultProviderId = id;
        }
        
        log.info("Registered model provider: {}", id);
    }

    /**
     * Unregister a model provider.
     *
     * @param providerId the provider id to unregister
     */
    public void unregister(String providerId) {
        ModelProvider removed = providers.remove(providerId);
        if (removed != null) {
            log.info("Unregistered model provider: {}", providerId);
            
            // Reset default if needed
            if (providerId.equals(defaultProviderId)) {
                defaultProviderId = providers.isEmpty() ? null : providers.keySet().iterator().next();
            }
        }
    }

    /**
     * Get a provider by id.
     *
     * @param providerId the provider id
     * @return the provider
     * @throws AiServiceException if provider not found
     */
    public ModelProvider getProvider(String providerId) {
        ModelProvider provider = providers.get(providerId);
        if (provider == null) {
            throw new AiServiceException(
                "PROVIDER_NOT_FOUND",
                "Model provider not found: " + providerId + ". Available: " + getAvailableProviders()
            );
        }
        return provider;
    }

    /**
     * Get the default provider.
     *
     * @return the default provider
     * @throws AiServiceException if no provider registered
     */
    public ModelProvider getDefaultProvider() {
        if (defaultProviderId == null) {
            throw new AiServiceException(
                "NO_PROVIDER_AVAILABLE",
                "No model provider registered. Please register at least one provider."
            );
        }
        return getProvider(defaultProviderId);
    }

    /**
     * Set the default provider.
     *
     * @param providerId the provider id to set as default
     */
    public void setDefaultProvider(String providerId) {
        if (!providers.containsKey(providerId)) {
            throw new AiServiceException(
                "PROVIDER_NOT_FOUND",
                "Cannot set default provider: " + providerId + " is not registered"
            );
        }
        this.defaultProviderId = providerId;
        log.info("Default provider set to: {}", providerId);
    }

    /**
     * Get all registered providers.
     *
     * @return unmodifiable list of providers
     */
    public List<ModelProvider> getAllProviders() {
        return Collections.unmodifiableList(
            providers.values().stream()
                .sorted((a, b) -> Integer.compare(b.getPriority(), a.getPriority()))
                .collect(Collectors.toList())
        );
    }

    /**
     * Get available provider ids.
     *
     * @return list of provider ids
     */
    public List<String> getAvailableProviders() {
        return Collections.unmodifiableList(
            providers.keySet().stream().sorted().collect(Collectors.toList())
        );
    }

    /**
     * Find providers that support a specific model.
     *
     * @param modelName the model name
     * @return list of supporting providers
     */
    public List<ModelProvider> findProvidersForModel(String modelName) {
        return providers.values().stream()
            .filter(p -> p.supportsModel(modelName))
            .sorted((a, b) -> Integer.compare(b.getPriority(), a.getPriority()))
            .collect(Collectors.toList());
    }

    /**
     * Check if a provider is registered.
     *
     * @param providerId the provider id
     * @return true if registered
     */
    public boolean hasProvider(String providerId) {
        return providers.containsKey(providerId);
    }

    /**
     * Get the count of registered providers.
     *
     * @return provider count
     */
    public int getProviderCount() {
        return providers.size();
    }

    /**
     * Clear all registered providers.
     */
    public void clear() {
        providers.clear();
        defaultProviderId = null;
        log.info("Cleared all model providers");
    }
}
