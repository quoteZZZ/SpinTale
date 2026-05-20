package com.spintale.ai.core.provider;

import com.spintale.ai.core.exception.AiServiceException;
import com.spintale.ai.core.service.AiChatService;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AI provider registry for Spring integration.
 * Manages AiModelProvider instances with routing capabilities.
 */
@Slf4j
public class AiProviderRegistry {

    private final Map<String, AiModelProvider> providers = new ConcurrentHashMap<>();
    private volatile String defaultProviderId;

    public AiProviderRegistry(List<AiModelProvider> providerList, String defaultProvider) {
        if (providerList != null) {
            providerList.forEach(this::register);
        }
        if (defaultProvider != null && providers.containsKey(defaultProvider)) {
            this.defaultProviderId = defaultProvider;
        }
    }

    public void register(AiModelProvider provider) {
        if (provider == null || !provider.isEnabled()) {
            return;
        }

        String id = provider.getProviderId();
        providers.put(id, provider);

        if (defaultProviderId == null) {
            defaultProviderId = id;
        }

        log.info("Registered AI provider: {}", id);
    }

    public AiModelProvider select(ChatRequestContext context) {
        if (providers.isEmpty()) {
            throw new AiServiceException("NO_PROVIDER", "No AI provider registered");
        }

        String providerId = context != null ? context.getProviderId() : null;
        if (providerId != null) {
            return getProvider(providerId);
        }

        return getDefaultProvider();
    }

    public AiModelProvider getProvider(String providerId) {
        AiModelProvider provider = providers.get(providerId);
        if (provider == null) {
            throw new AiServiceException("PROVIDER_NOT_FOUND", 
                "Provider not found: " + providerId + ". Available: " + getAvailableProviders());
        }
        return provider;
    }

    public AiModelProvider getDefaultProvider() {
        if (defaultProviderId == null) {
            throw new AiServiceException("NO_DEFAULT_PROVIDER", "No default provider set");
        }
        return getProvider(defaultProviderId);
    }

    public List<String> getAvailableProviders() {
        return new ArrayList<>(providers.keySet());
    }

    public ChatRequestContext createRequestContext() {
        return new ChatRequestContext();
    }

    public static class ChatRequestContext {
        private String providerId;
        private String model;
        private Map<String, Object> attributes = new HashMap<>();

        public String getProviderId() { return providerId; }
        public ChatRequestContext providerId(String providerId) {
            this.providerId = providerId;
            return this;
        }

        public String getModel() { return model; }
        public ChatRequestContext model(String model) {
            this.model = model;
            return this;
        }

        public Map<String, Object> getAttributes() { return attributes; }
        public ChatRequestContext attribute(String key, Object value) {
            this.attributes.put(key, value);
            return this;
        }
    }
}
