package com.spintale.ai.core.provider;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import com.spintale.ai.core.model.ChatRequest;

/**
 * Ordered registry used by the AI gateway to select a provider.
 */
public class AiProviderRegistry {

    private final List<AiModelProvider> providers;
    private final String defaultProviderId;

    public AiProviderRegistry(List<AiModelProvider> providers, String defaultProviderId) {
        this.providers = new ArrayList<>(providers == null ? List.of() : providers);
        this.providers.sort(Comparator.comparingInt(AiModelProvider::getOrder));
        this.defaultProviderId = defaultProviderId;
    }

    public AiModelProvider select(ChatRequest request) {
        String requestedProviderId = getRequestedProviderId(request);
        if (requestedProviderId != null) {
            return findById(requestedProviderId)
                    .orElseThrow(() -> new IllegalArgumentException("Unknown AI provider: " + requestedProviderId));
        }

        Optional<AiModelProvider> defaultProvider = findById(defaultProviderId);
        if (defaultProvider.isPresent() && defaultProvider.get().supports(request)) {
            return defaultProvider.get();
        }

        return providers.stream()
                .filter(provider -> provider.supports(request))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No AI model provider is available"));
    }

    public Optional<AiModelProvider> findById(String providerId) {
        if (providerId == null || providerId.isBlank()) {
            return Optional.empty();
        }
        return providers.stream()
                .filter(provider -> provider.getId().equalsIgnoreCase(providerId))
                .findFirst();
    }

    public List<AiModelProvider> listProviders() {
        return List.copyOf(providers);
    }

    private String getRequestedProviderId(ChatRequest request) {
        if (request == null || request.getExtraParams() == null) {
            return null;
        }
        Object provider = request.getExtraParams().get("provider");
        if (provider == null) {
            return null;
        }
        String providerId = String.valueOf(provider).trim();
        return providerId.isEmpty() ? null : providerId;
    }
}
