package com.spintale.ai.providers.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.spintale.ai.core.model.ChatRequest;
import com.spintale.ai.core.model.ChatResponse;
import com.spintale.ai.core.api.AiChatService;
import com.spintale.ai.infrastructure.adapter.LangChain4jAdapter;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;

public class LangChain4jChatService implements AiChatService
{
    private static final Logger log = LoggerFactory.getLogger(LangChain4jChatService.class);

    private final ChatModel chatModel;
    private final StreamingChatModel streamingChatModel;

    public LangChain4jChatService(ChatModel chatModel)
    {
        this(chatModel, null);
    }

    public LangChain4jChatService(ChatModel chatModel, StreamingChatModel streamingChatModel)
    {
        this.chatModel = chatModel;
        this.streamingChatModel = streamingChatModel;
    }

    @Override
    public String chat(String message)
    {
        return chatModel.chat(message);
    }

    @Override
    public ChatResponse chat(ChatRequest request)
    {
        dev.langchain4j.model.chat.response.ChatResponse response =
                chatModel.chat(LangChain4jAdapter.convertMessages(request));
        return LangChain4jAdapter.convertChatResponse(response, request == null ? null : request.getSessionId());
    }

    @Override
    public void streamChat(ChatRequest request, StreamHandler handler)
    {
        if (streamingChatModel != null) {
            // 真正的流式处理
            try {
                StringBuilder contentBuilder = new StringBuilder();
                
                streamingChatModel.chat(
                    LangChain4jAdapter.convertMessages(request),
                    new dev.langchain4j.model.chat.response.StreamingChatResponseHandler() {
                        @Override
                        public void onPartialResponse(String token) {
                            contentBuilder.append(token);
                            handler.onToken(token);
                        }

                        @Override
                        public void onCompleteResponse(dev.langchain4j.model.chat.response.ChatResponse response) {
                            ChatResponse chatResponse = LangChain4jAdapter.convertChatResponse(
                                response, 
                                request == null ? null : request.getSessionId()
                            );
                            handler.onComplete(chatResponse);
                        }

                        @Override
                        public void onError(Throwable error) {
                            handler.onError(error);
                        }
                    }
                );
            } catch (Exception e) {
                log.error("Streaming chat failed", e);
                handler.onError(e);
            }
        } else {
            // 降级为同步处理
            log.debug("Streaming not available, falling back to sync response");
            try {
                ChatResponse response = chat(request);
                handler.onToken(response.getContent());
                handler.onComplete(response);
            } catch (Exception e) {
                handler.onError(e);
            }
        }
    }
}
