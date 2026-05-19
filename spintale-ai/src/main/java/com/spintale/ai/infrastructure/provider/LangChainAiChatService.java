package com.spintale.ai.infrastructure.provider;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.spintale.ai.core.service.AiChatService;
import com.spintale.ai.core.model.ChatRequest;
import com.spintale.ai.core.model.ChatResponse;
import com.spintale.ai.core.model.StreamHandler;
import com.spintale.ai.core.model.TokenUsage;
import com.spintale.common.utils.StringUtils;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;

public class LangChainAiChatService implements AiChatService
{
    private static final Logger log = LoggerFactory.getLogger(LangChainAiChatService.class);

    private final ChatModel chatModel;

    public LangChainAiChatService(ChatModel chatModel)
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
        dev.langchain4j.model.chat.response.ChatResponse response = chatModel.chat(buildMessages(request));
        dev.langchain4j.model.output.TokenUsage usage = response.tokenUsage();
        return ChatResponse.builder()
                .sessionId(request.getSessionId())
                .content(response.aiMessage().text())
                .model(response.modelName())
                .tokenUsage(TokenUsage.builder()
                        .promptTokens(usage == null ? 0 : usage.inputTokenCount())
                        .completionTokens(usage == null ? 0 : usage.outputTokenCount())
                        .totalTokens(usage == null ? 0 : usage.totalTokenCount())
                        .build())
                .finished(true)
                .build();
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

    private List<dev.langchain4j.data.message.ChatMessage> buildMessages(ChatRequest request)
    {
        List<dev.langchain4j.data.message.ChatMessage> messages = new ArrayList<>();
        if (request.getMessages() != null && !request.getMessages().isEmpty())
        {
            for (com.spintale.ai.core.model.ChatMessage message : request.getMessages())
            {
                messages.add(convert(message));
            }
            return messages;
        }
        if (StringUtils.isNotBlank(request.getSystemPrompt()))
        {
            messages.add(SystemMessage.from(request.getSystemPrompt()));
        }
        if (request.getHistory() != null)
        {
            for (com.spintale.ai.core.model.ChatMessage message : request.getHistory())
            {
                messages.add(convert(message));
            }
        }
        messages.add(UserMessage.from(StringUtils.defaultString(request.getMessage())));
        return messages;
    }

    private dev.langchain4j.data.message.ChatMessage convert(com.spintale.ai.core.model.ChatMessage message)
    {
        if ("system".equalsIgnoreCase(message.getRole()))
        {
            return SystemMessage.from(message.getContent());
        }
        if ("assistant".equalsIgnoreCase(message.getRole()))
        {
            return AiMessage.from(message.getContent());
        }
        return UserMessage.from(message.getContent());
    }
}
