package com.spintale.ai.core.spi;

import com.spintale.ai.core.exception.AiServiceException;
import com.spintale.ai.core.model.ChatRequest;
import com.spintale.ai.core.model.ChatResponse;

/**
 * Embedding model SPI interface.
 * All embedding providers must implement this interface.
 */
public interface EmbeddingModel {

    /**
     * Generate embeddings for the given text.
     *
     * @param text input text
     * @return embedding vector
     * @throws AiServiceException if embedding generation fails
     */
    float[] embed(String text);

    /**
     * Get the provider identifier.
     *
     * @return provider ID (e.g., "openai", "ollama")
     */
    String getProviderId();

    /**
     * Get the model name.
     *
     * @return model name (e.g., "text-embedding-3-small")
     */
    String getModelName();
}
