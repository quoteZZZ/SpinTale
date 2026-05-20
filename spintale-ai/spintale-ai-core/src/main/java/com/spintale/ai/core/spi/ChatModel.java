package com.spintale.ai.core.spi;

import com.spintale.ai.core.model.ChatRequest;
import com.spintale.ai.core.model.ChatResponse;

/**
 * Core abstraction for chat models.
 * All model providers must implement this interface.
 */
public interface ChatModel {

    /**
     * Execute a chat request and return response.
     *
     * @param request chat request with messages and configuration
     * @return chat response with content and metadata
     */
    ChatResponse chat(ChatRequest request);

    /**
     * Execute a streaming chat request.
     *
     * @param request chat request with messages and configuration
     * @param handler callback handler for streaming tokens
     */
    void streamChat(ChatRequest request, StreamHandler handler);

    /**
     * Get the provider identifier.
     *
     * @return provider id (e.g., "openai", "ollama", "anthropic")
     */
    String getProviderId();

    /**
     * Get the default model name.
     *
     * @return model name
     */
    String getModelName();

    /**
     * Streaming response handler.
     */
    @FunctionalInterface
    interface StreamHandler {
        /**
         * Called when a token is received.
         *
         * @param token partial token text
         */
        void onToken(String token);

        /**
         * Called when the stream is complete.
         *
         * @param response final chat response
         */
        default void onComplete(ChatResponse response) {
        }

        /**
         * Called when an error occurs.
         *
         * @param error the error
         */
        default void onError(Throwable error) {
        }
    }
}
