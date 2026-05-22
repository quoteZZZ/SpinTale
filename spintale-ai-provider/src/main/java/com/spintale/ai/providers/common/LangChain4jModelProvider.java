package com.spintale.ai.providers.common;

import java.util.Map;

import com.spintale.ai.core.provider.AiModelProvider;
import com.spintale.ai.core.service.AiChatService;
import com.spintale.ai.core.spi.ModelProvider;

import dev.langchain4j.model.chat.ChatModel;

/**
 * LangChain4j-backed provider adapter.
 */
public class LangChain4jModelProvider implements AiModelProvider {

    private final String id;
    private final AiModelProvider.Type type;
    private final ChatModel chatModel;
    private final AiChatService chatService;

    public LangChain4jModelProvider(String id, AiModelProvider.Type type, ChatModel chatModel) {
        this.id = id;
        this.type = type;
        this.chatModel = chatModel;
        this.chatService = new LangChain4jChatService(chatModel);
    }

    @Override
    public String getProviderId() {
        return id;
    }

    @Override
    public AiModelProvider.Type getType() {
        return type;
    }

    @Override
    public ModelProvider getModelProvider() {
        return null;
    }

    @Override
    public AiChatService getChatService() {
        return chatService;
    }

    @Override
    public Map<String, Object> getMetadata() {
        return Map.of(
                "adapter", "langchain4j",
                "chatModelClass", chatModel.getClass().getName()
        );
    }
}
