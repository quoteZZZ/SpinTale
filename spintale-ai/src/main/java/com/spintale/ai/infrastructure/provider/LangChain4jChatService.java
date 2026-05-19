package com.spintale.ai.infrastructure.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.spintale.ai.core.model.ChatRequest;
import com.spintale.ai.core.model.ChatResponse;
import com.spintale.ai.core.service.AiChatService;
import com.spintale.ai.infrastructure.adapter.LangChain4jAdapter;

import dev.langchain4j.model.chat.ChatModel;

public class LangChain4jChatService implements AiChatService
{
    private static final Logger log = LoggerFactory.getLogger(LangChain4jChatService.class);

    private final ChatModel chatModel;

    public LangChain4jChatService(ChatModel chatModel)
    {
        this.chatModel = chatModel;
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
        log.debug("Streaming is not enabled for this provider bean; falling back to a single response.");
        try {
            ChatResponse response = chat(request);
            handler.onComplete(response);
        } catch (Exception e) {
            handler.onError(e);
        }
    }
}
