package com.spintale.ai.core.spi;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Model provider abstraction.
 * Represents a specific AI model provider (e.g., OpenAI, Ollama).
 */
public class ModelProvider {

    private final String id;
    private final String name;
    private final ChatModel chatModel;
    private final EmbeddingModel embeddingModel;

    public ModelProvider(String id, String name, ChatModel chatModel) {
        this(id, name, chatModel, null);
    }

    public ModelProvider(String id, String name, ChatModel chatModel, EmbeddingModel embeddingModel) {
        this.id = id;
        this.name = name;
        this.chatModel = chatModel;
        this.embeddingModel = embeddingModel;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ChatModel getChatModel() {
        return chatModel;
    }

    public EmbeddingModel getEmbeddingModel() {
        return embeddingModel;
    }

    public boolean hasChatCapability() {
        return chatModel != null;
    }

    public boolean hasEmbeddingCapability() {
        return embeddingModel != null;
    }

    /**
     * Check if this provider supports the requested model.
     *
     * @param modelName model name to check
     * @return true if supported
     */
    public boolean supportsModel(String modelName) {
        // Default implementation: all providers support all models
        // Override in subclasses for specific model support
        return true;
    }

    /**
     * Get provider priority for routing (higher = preferred).
     *
     * @return priority value
     */
    public int getPriority() {
        return 0;
    }

    /**
     * Simple model provider implementation for quick registration.
     */
    public static class SimpleModelProvider extends ModelProvider {
        public SimpleModelProvider(String id, ChatModel chatModel) {
            super(id, id, chatModel);
        }

        public SimpleModelProvider(String id, ChatModel chatModel, EmbeddingModel embeddingModel) {
            super(id, id, chatModel, embeddingModel);
        }
    }
}
