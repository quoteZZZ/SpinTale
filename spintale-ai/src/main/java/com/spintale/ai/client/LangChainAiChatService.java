package com.spintale.ai.client;

import com.spintale.ai.core.*;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.output.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 基于 LangChain4j 的 AI 聊天服务实现
 */
@Slf4j
public class LangChainAiChatService implements AiChatService {
    
    private final ChatLanguageModel chatModel;
    
    public LangChainAiChatService(ChatLanguageModel chatModel) {
        this.chatModel = chatModel;
    }
    
    @Override
    public String chat(String message) {
        log.debug("Sending message to AI: {}", message);
        Response<AiMessage> response = chatModel.generate(message);
        return response.content().text();
    }
    
    @Override
    public ChatResponse chat(ChatRequest request) {
        log.debug("Processing chat request with session: {}", request.getSessionId());
        
        // 构建消息列表
        List<ChatMessage> messages = buildMessages(request);
        
        // 调用模型
        Response<AiMessage> response = chatModel.generate(messages);
        
        // 构建响应
        return ChatResponse.builder()
                .sessionId(request.getSessionId())
                .content(response.content().text())
                .tokenUsage(TokenUsage.builder()
                        .promptTokens(response.tokenUsage().promptTokenCount())
                        .completionTokens(response.tokenUsage().completionTokenCount())
                        .totalTokens(response.tokenUsage().totalTokenCount())
                        .build())
                .finished(true)
                .build();
    }
    
    @Override
    public void streamChat(ChatRequest request, StreamHandler handler) {
        // TODO: 实现流式聊天
        log.warn("Stream chat not yet implemented");
        ChatResponse response = chat(request);
        handler.onComplete(response);
    }
    
    /**
     * 构建消息列表
     */
    private List<ChatMessage> buildMessages(ChatRequest request) {
        List<ChatMessage> messages = new ArrayList<>();
        
        // 添加系统提示词
        if (StringUtils.hasText(request.getSystemPrompt())) {
            messages.add(SystemMessage.from(request.getSystemPrompt()));
        }
        
        // 添加历史消息
        if (request.getHistory() != null) {
            List<ChatMessage> historyMessages = request.getHistory().stream()
                    .map(this::convertToLangChainMessage)
                    .collect(Collectors.toList());
            messages.addAll(historyMessages);
        }
        
        // 添加当前用户消息
        messages.add(UserMessage.from(request.getMessage()));
        
        return messages;
    }
    
    /**
     * 转换为 LangChain4j 消息
     */
    private ChatMessage convertToLangChainMessage(com.spintale.ai.core.ChatMessage msg) {
        String role = msg.getRole();
        String content = msg.getContent();
        
        switch (role) {
            case "system":
                return SystemMessage.from(content);
            case "user":
                return UserMessage.from(content);
            case "assistant":
                return AiMessage.from(content);
            default:
                return UserMessage.from(content);
        }
    }
}
