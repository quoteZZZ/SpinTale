package com.spintale.ai.providers.common;

import java.util.List;

import com.spintale.ai.core.model.ChatResponse;
import com.spintale.ai.core.model.MultimodalMessage;
import com.spintale.ai.core.service.MultimodalChatService;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;

public class LangChain4jMultimodalService implements MultimodalChatService {

    private final ChatModel chatModel;
    private final StreamingChatModel streamingChatModel;

    public LangChain4jMultimodalService(ChatModel chatModel) {
        this(chatModel, null);
    }

    public LangChain4jMultimodalService(ChatModel chatModel, StreamingChatModel streamingChatModel) {
        this.chatModel = chatModel;
        this.streamingChatModel = streamingChatModel;
    }

    @Override
    public ChatResponse chat(List<MultimodalMessage> messages) {
        String text = messages == null || messages.isEmpty()
                ? ""
                : messages.get(messages.size() - 1).getTextContent();
        String content = chatModel.chat(text == null ? "" : text);
        return ChatResponse.builder().content(content).finished(true).build();
    }

    @Override
    public ChatResponse chatWithImage(String text, String imageUrl) {
        return chat(List.of(MultimodalMessage.userImage(text, imageUrl)));
    }

    @Override
    public ChatResponse chatWithImages(String text, List<String> imageUrls) {
        return chat(List.of(MultimodalMessage.userImages(text, imageUrls)));
    }

    @Override
    public ChatResponse chatWithAudio(String text, String audioUrl) {
        return chat(List.of(MultimodalMessage.userAudio(text, audioUrl)));
    }

    @Override
    public void streamChat(List<MultimodalMessage> messages, StreamHandler handler) {
        ChatResponse response = chat(messages);
        handler.onToken(response.getContent());
        handler.onComplete(response);
    }
}
