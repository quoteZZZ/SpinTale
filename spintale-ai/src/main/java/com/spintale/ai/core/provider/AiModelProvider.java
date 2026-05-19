package com.spintale.ai.core.provider;

import java.util.Map;

import com.spintale.ai.core.model.ChatRequest;
import com.spintale.ai.core.service.AiChatService;

/**
 * Pluggable model provider contract.
 */
public interface AiModelProvider {

    String getId();

    Type getType();

    AiChatService getChatService();

    default int getOrder() {
        return 0;
    }

    default boolean supports(ChatRequest request) {
        if (request == null || request.getExtraParams() == null) {
            return true;
        }
        Object provider = request.getExtraParams().get("provider");
        return provider == null || getId().equalsIgnoreCase(String.valueOf(provider));
    }

    default Map<String, Object> getMetadata() {
        return Map.of();
    }

    enum Type {
        LANGCHAIN4J,
        SPRING_AI,
        SEMANTIC_KERNEL,
        DJL,
        OPENAI_COMPATIBLE,
        CUSTOM
    }
}
