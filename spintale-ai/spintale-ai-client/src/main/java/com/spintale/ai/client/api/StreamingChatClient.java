package com.spintale.ai.client.api;

import com.spintale.ai.core.model.ChatRequest;
import com.spintale.ai.core.spi.ChatModel;
import lombok.extern.slf4j.Slf4j;

/**
 * Streaming chat client for real-time token streaming.
 */
@Slf4j
public class StreamingChatClient {

    private final ChatModel chatModel;

    public StreamingChatClient(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    /**
     * Stream chat response tokens.
     *
     * @param request chat request
     * @param onToken callback for each token
     * @param onComplete callback when streaming completes
     */
    public void stream(ChatRequest request, 
                      java.util.function.Consumer<String> onToken,
                      java.util.function.Consumer<com.spintale.ai.core.model.ChatResponse> onComplete) {
        
        chatModel.streamChat(request, new ChatModel.StreamHandler() {
            @Override
            public void onToken(String token) {
                onToken.accept(token);
            }

            @Override
            public void onComplete(com.spintale.ai.core.model.ChatResponse response) {
                onComplete.accept(response);
            }

            @Override
            public void onError(Throwable error) {
                log.error("Streaming chat error", error);
            }
        });
    }
}
