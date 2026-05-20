package com.spintale.ai.core.service;

import com.spintale.ai.core.model.ChatRequest;
import com.spintale.ai.core.model.ChatResponse;

/**
 * Core AI chat service interface.
 * Provider-agnostic chat service abstraction.
 */
public interface AiChatService {

    /**
     * Simple chat with a message string.
     *
     * @param message the message
     * @return the response content
     */
    String chat(String message);

    /**
     * Chat with a full request object.
     *
     * @param request the chat request
     * @return the chat response
     */
    ChatResponse chat(ChatRequest request);

    /**
     * Streaming chat.
     *
     * @param request the chat request
     * @param handler the stream handler
     */
    void streamChat(ChatRequest request, StreamHandler handler);

    /**
     * Stream handler interface.
     */
    interface StreamHandler {
        void onToken(String token);
        void onComplete(ChatResponse response);
        void onError(Throwable error);
    }
}
