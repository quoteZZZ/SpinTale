package com.spintale.ai.infrastructure.provider;

import java.util.Map;

import com.spintale.ai.core.provider.AiModelProvider;
import com.spintale.ai.core.api.AiChatService;

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
    public String getId() {
        return id;
    }

    @Override
    public AiModelProvider.Type getType() {
        return type;
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
