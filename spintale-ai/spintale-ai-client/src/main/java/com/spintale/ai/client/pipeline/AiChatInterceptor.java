package com.spintale.ai.client.pipeline;

import com.spintale.ai.core.model.ChatRequest;
import com.spintale.ai.core.model.ChatResponse;

/**
 * Lightweight chat pipeline hook inspired by Spring AI Advisors and gateway filters.
 */
public interface AiChatInterceptor {

    default int getOrder() {
        return 0;
    }

    default ChatRequest beforeChat(ChatRequest request) {
        return request;
    }

    default ChatResponse afterChat(ChatRequest request, ChatResponse response) {
        return response;
    }

    default void onError(ChatRequest request, Throwable error) {
    }
}
