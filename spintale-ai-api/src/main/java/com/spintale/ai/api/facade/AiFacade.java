package com.spintale.ai.api.facade;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.spintale.ai.api.api.ChatClient;
import com.spintale.ai.core.model.ChatMessage;
import com.spintale.ai.core.spi.ChatModel;

/**
 * Lightweight facade for model-only chat use cases.
 */
public class AiFacade {

    private final ChatModel chatModel;

    public AiFacade(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    public String chat(String message) {
        return ChatClient.create(chatModel).user(message).call().getContent();
    }

    public String chat(String systemMessage, String userMessage) {
        return ChatClient.create(chatModel)
                .system(systemMessage)
                .user(userMessage)
                .call()
                .getContent();
    }

    public String chat(String message, List<Map<String, String>> context) {
        ChatClient client = ChatClient.create(chatModel);
        if (context != null) {
            for (Map<String, String> item : context) {
                String role = item.get("role");
                String content = item.get("content");
                if (ChatMessage.ROLE_ASSISTANT.equals(role)) {
                    client.assistant(content);
                } else if (ChatMessage.ROLE_SYSTEM.equals(role)) {
                    client.system(content);
                } else {
                    client.user(content);
                }
            }
        }
        return client.user(message).call().getContent();
    }

    public void chatStreaming(String message, Consumer<String> tokenConsumer) {
        ChatClient.create(chatModel).user(message).stream(new ChatModel.StreamHandler() {
            @Override
            public void onToken(String token) {
                tokenConsumer.accept(token);
            }
        });
    }

    public Map<String, Object> getStats() {
        return Map.of(
                "providerId", chatModel.getProviderId(),
                "modelName", chatModel.getModelName());
    }
}
