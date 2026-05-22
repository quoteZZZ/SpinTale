package com.spintale.ai.runtime.provider.langchain4j;

import com.spintale.ai.core.exception.AiServiceException;
import com.spintale.ai.core.model.ChatMessage;
import com.spintale.ai.core.model.ChatRequest;
import com.spintale.ai.core.model.ChatResponse;
import com.spintale.ai.core.model.TokenUsage;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

/**
 * LangChain4j adapter for ChatModel SPI.
 */
@Slf4j
public class LangChain4jChatModelAdapter implements com.spintale.ai.core.spi.ChatModel {

    private final String providerId;
    private final String modelName;
    private final dev.langchain4j.model.chat.ChatModel chatModel;
    private final dev.langchain4j.model.chat.StreamingChatModel streamingChatModel;

    public LangChain4jChatModelAdapter(String providerId, String modelName, 
                                       dev.langchain4j.model.chat.ChatModel chatModel) {
        this(providerId, modelName, chatModel, null);
    }

    public LangChain4jChatModelAdapter(String providerId, String modelName, 
                                       dev.langchain4j.model.chat.ChatModel chatModel, 
                                       dev.langchain4j.model.chat.StreamingChatModel streamingChatModel) {
        this.providerId = providerId;
        this.modelName = modelName;
        this.chatModel = chatModel;
        this.streamingChatModel = streamingChatModel;
    }

    @Override
    public ChatResponse chat(ChatRequest request) {
        try {
            // Convert SpinTale messages to LangChain4j messages
            List<dev.langchain4j.data.message.ChatMessage> lcMessages = convertMessages(request.getMessages());

            // Build LangChain4j chat request
            dev.langchain4j.model.chat.request.ChatRequest lcRequest = 
                dev.langchain4j.model.chat.request.ChatRequest.builder()
                    .messages(lcMessages)
                    .build();

            // Execute chat
            dev.langchain4j.model.chat.response.ChatResponse lcResponse = chatModel.chat(lcRequest);

            // Convert response
            return convertResponse(lcResponse);

        } catch (Exception e) {
            log.error("LangChain4j chat failed", e);
            throw new AiServiceException("CHAT_FAILED", "Chat execution failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void streamChat(ChatRequest request, com.spintale.ai.core.spi.ChatModel.StreamHandler handler) {
        if (streamingChatModel == null) {
            throw new AiServiceException("STREAMING_NOT_SUPPORTED", 
                "Streaming is not supported by this model");
        }

        try {
            List<dev.langchain4j.data.message.ChatMessage> lcMessages = convertMessages(request.getMessages());

            dev.langchain4j.model.chat.request.ChatRequest lcRequest = 
                dev.langchain4j.model.chat.request.ChatRequest.builder()
                    .messages(lcMessages)
                    .build();

            streamingChatModel.chat(lcRequest, new dev.langchain4j.model.chat.response.StreamingChatResponseHandler() {
                @Override
                public void onPartialResponse(String token) {
                    handler.onToken(token);
                }

                @Override
                public void onCompleteResponse(dev.langchain4j.model.chat.response.ChatResponse response) {
                    ChatResponse chatResponse = convertResponse(response);
                    handler.onComplete(chatResponse);
                }

                @Override
                public void onError(Throwable error) {
                    handler.onError(error);
                }
            });

        } catch (Exception e) {
            log.error("LangChain4j streaming chat failed", e);
            handler.onError(new AiServiceException("STREAM_CHAT_FAILED", 
                "Streaming chat failed: " + e.getMessage(), e));
        }
    }

    @Override
    public String getProviderId() {
        return providerId;
    }

    @Override
    public String getModelName() {
        return modelName;
    }

    /**
     * Convert SpinTale messages to LangChain4j messages.
     */
    private List<dev.langchain4j.data.message.ChatMessage> convertMessages(List<ChatMessage> messages) {
        return messages.stream()
            .map(this::convertMessage)
            .collect(Collectors.toList());
    }

    /**
     * Convert a single message.
     */
    private dev.langchain4j.data.message.ChatMessage convertMessage(ChatMessage message) {
        return switch (message.getRole()) {
            case "system" -> new SystemMessage(message.getContent());
            case "user" -> new UserMessage(message.getContent());
            case "assistant" -> new AiMessage(message.getContent());
            default -> throw new IllegalArgumentException("Unsupported role: " + message.getRole());
        };
    }

    /**
     * Convert LangChain4j response to SpinTale response.
     */
    private ChatResponse convertResponse(dev.langchain4j.model.chat.response.ChatResponse lcResponse) {
        AiMessage aiMessage = lcResponse.aiMessage();
        
        TokenUsage tokenUsage = null;
        if (lcResponse.tokenUsage() != null) {
            tokenUsage = new TokenUsage(
                lcResponse.tokenUsage().inputTokenCount(),
                lcResponse.tokenUsage().outputTokenCount(),
                lcResponse.tokenUsage().totalTokenCount()
            );
        }

        return ChatResponse.builder()
            .content(aiMessage.text())
            .tokenUsage(tokenUsage)
            .finishReason(lcResponse.finishReason() != null ? lcResponse.finishReason().toString() : "stop")
            .build();
    }
}
