package com.spintale.ai.core.service;

import com.spintale.ai.core.model.MultimodalMessage;
import com.spintale.ai.core.model.ChatResponse;

import java.util.List;

public interface MultimodalChatService {
    
    ChatResponse chat(List<MultimodalMessage> messages);
    
    ChatResponse chatWithImage(String text, String imageUrl);
    
    ChatResponse chatWithImages(String text, List<String> imageUrls);
    
    ChatResponse chatWithAudio(String text, String audioUrl);
    
    void streamChat(List<MultimodalMessage> messages, StreamHandler handler);

    interface StreamHandler {
        void onToken(String token);

        default void onComplete(ChatResponse response) {
        }

        default void onError(Throwable error) {
        }
    }
}
