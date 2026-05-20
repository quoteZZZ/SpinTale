package com.spintale.ai.core.provider;

import com.spintale.ai.core.service.AiChatService;
import com.spintale.ai.core.spi.ModelProvider;

/**
 * AI model provider interface for Spring integration.
 * Bridges ModelProvider with Spring's dependency injection.
 */
public interface AiModelProvider {

    /**
     * Get the provider ID.
     *
     * @return provider ID
     */
    String getProviderId();

    /**
     * Get the underlying model provider.
     *
     * @return model provider
     */
    ModelProvider getModelProvider();

    /**
     * Get the chat service.
     *
     * @return chat service
     */
    AiChatService getChatService();

    /**
     * Check if this provider is enabled.
     *
     * @return true if enabled
     */
    default boolean isEnabled() {
        return true;
    }

    /**
     * Get provider priority for routing.
     *
     * @return priority (higher = preferred)
     */
    default int getPriority() {
        return 0;
    }
}
