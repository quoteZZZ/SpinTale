package com.spintale.ai.runtime.api;

import com.spintale.ai.core.model.ChatMessage;
import com.spintale.ai.core.model.ChatRequest;
import com.spintale.ai.core.model.ChatResponse;
import com.spintale.ai.core.options.ChatOptions;
import com.spintale.ai.core.spi.ChatModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Fluent API for AI chat interactions.
 * 
 * <p>Example usage:</p>
 * <pre>{@code
 * ChatResponse response = ChatClient.create(chatModel)
 *     .system("You are a helpful assistant")
 *     .user("What is Java?")
 *     .options(ChatOptions.builder().temperature(0.7).build())
 *     .call();
 * }</pre>
 */
public class ChatClient {

    private final ChatModel chatModel;
    private final List<ChatMessage> messages = new ArrayList<>();
    private ChatOptions options;

    private ChatClient(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    /**
     * Create a new ChatClient instance.
     *
     * @param chatModel the chat model to use
     * @return new ChatClient instance
     */
    public static ChatClient create(ChatModel chatModel) {
        return new ChatClient(chatModel);
    }

    /**
     * Add a system message.
     *
     * @param content system message content
     * @return this client for chaining
     */
    public ChatClient system(String content) {
        messages.add(ChatMessage.system(content));
        return this;
    }

    /**
     * Add a user message.
     *
     * @param content user message content
     * @return this client for chaining
     */
    public ChatClient user(String content) {
        messages.add(ChatMessage.user(content));
        return this;
    }

    /**
     * Add an assistant message.
     *
     * @param content assistant message content
     * @return this client for chaining
     */
    public ChatClient assistant(String content) {
        messages.add(ChatMessage.assistant(content));
        return this;
    }

    /**
     * Add custom messages.
     *
     * @param messages list of messages to add
     * @return this client for chaining
     */
    public ChatClient messages(List<ChatMessage> messages) {
        this.messages.addAll(messages);
        return this;
    }

    /**
     * Set chat options.
     *
     * @param options chat configuration options
     * @return this client for chaining
     */
    public ChatClient options(ChatOptions options) {
        this.options = options;
        return this;
    }

    /**
     * Execute the chat request and get response.
     *
     * @return chat response
     */
    public ChatResponse call() {
        ChatRequest request = buildRequest();
        return chatModel.chat(request);
    }

    /**
     * Execute the chat request with streaming.
     *
     * @param handler streaming response handler
     */
    public void stream(ChatModel.StreamHandler handler) {
        ChatRequest request = buildRequest();
        request.setStreaming(true);
        chatModel.streamChat(request, handler);
    }

    /**
     * Build chat request from current state.
     *
     * @return chat request
     */
    private ChatRequest buildRequest() {
        if (messages.isEmpty()) {
            throw new IllegalStateException("At least one message is required");
        }

        ChatRequest.ChatRequestBuilder builder = ChatRequest.builder()
                .messages(new ArrayList<>(messages));

        if (options != null) {
            builder.model(options.getModel())
                    .temperature(options.getTemperature())
                    .maxTokens(options.getMaxTokens());
        }

        return builder.build();
    }

    /**
     * Clear all messages and reset state.
     *
     * @return this client for chaining
     */
    public ChatClient clear() {
        messages.clear();
        options = null;
        return this;
    }
}
