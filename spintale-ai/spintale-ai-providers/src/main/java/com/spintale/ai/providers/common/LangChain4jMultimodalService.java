package com.spintale.ai.providers.common;

import com.spintale.ai.core.model.ChatResponse;
import com.spintale.ai.core.model.MultimodalMessage;
import com.spintale.ai.core.model.MediaContent;
import com.spintale.ai.core.service.MultimodalChatService;
import com.spintale.ai.core.service.StreamHandler;
import dev.langchain4j.data.message.*;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class LangChain4jMultimodalService implements MultimodalChatService {
    
    private static final Logger log = LoggerFactory.getLogger(LangChain4jMultimodalService.class);
    
    private final ChatModel chatModel;
    private final StreamingChatModel streamingChatModel;
    
    public LangChain4jMultimodalService(ChatModel chatModel) {
        this(chatModel, null);
    }
    
    public LangChain4jMultimodalService(ChatModel chatModel, StreamingChatModel streamingChatModel) {
        this.chatModel = chatModel;
        this.streamingChatModel = streamingChatModel;
    }
    
    @Override
    public ChatResponse chat(List<MultimodalMessage> messages) {
        List<ChatMessage> langchain4jMessages = convertMessages(messages);
        
        dev.langchain4j.model.output.Response<AiMessage> response = chatModel.generate(langchain4jMessages);
        
        return ChatResponse.builder()
                .content(response.content().text())
                .tokenUsage(new com.spintale.ai.core.model.TokenUsage(
                        response.tokenUsage() != null ? response.tokenUsage().inputTokenCount() : 0,
                        response.tokenUsage() != null ? response.tokenUsage().outputTokenCount() : 0,
                        response.tokenUsage() != null ? response.tokenUsage().totalTokenCount() : 0
                ))
                .finishReason(response.finishReason() != null ? response.finishReason().name() : null)
                .build();
    }
    
    @Override
    public ChatResponse chatWithImage(String text, String imageUrl) {
        List<MultimodalMessage> messages = List.of(MultimodalMessage.userImage(text, imageUrl));
        return chat(messages);
    }
    
    @Override
    public ChatResponse chatWithImages(String text, List<String> imageUrls) {
        List<MultimodalMessage> messages = List.of(MultimodalMessage.userImages(text, imageUrls));
        return chat(messages);
    }
    
    @Override
    public ChatResponse chatWithAudio(String text, String audioUrl) {
        List<MultimodalMessage> messages = List.of(MultimodalMessage.userAudio(text, audioUrl));
        return chat(messages);
    }
    
    @Override
    public void streamChat(List<MultimodalMessage> messages, StreamHandler handler) {
        if (streamingChatModel == null) {
            ChatResponse response = chat(messages);
            handler.onComplete(response);
            return;
        }
        
        List<ChatMessage> langchain4jMessages = convertMessages(messages);
        
        StringBuilder contentBuilder = new StringBuilder();
        
        streamingChatModel.generate(langchain4jMessages, new dev.langchain4j.model.output.StreamingResponseHandler<AiMessage>() {
            @Override
            public void onNext(String token) {
                contentBuilder.append(token);
                handler.onNext(token);
            }
            
            @Override
            public void onComplete(dev.langchain4j.model.output.Response<AiMessage> response) {
                ChatResponse chatResponse = ChatResponse.builder()
                        .content(contentBuilder.toString())
                        .tokenUsage(new com.spintale.ai.core.model.TokenUsage(
                                response.tokenUsage() != null ? response.tokenUsage().inputTokenCount() : 0,
                                response.tokenUsage() != null ? response.tokenUsage().outputTokenCount() : 0,
                                response.tokenUsage() != null ? response.tokenUsage().totalTokenCount() : 0
                        ))
                        .finished(true)
                        .build();
                
                handler.onComplete(chatResponse);
            }
            
            @Override
            public void onError(Throwable error) {
                handler.onError(error);
            }
        });
    }
    
    private List<ChatMessage> convertMessages(List<MultimodalMessage> messages) {
        List<ChatMessage> result = new ArrayList<>();
        
        for (MultimodalMessage msg : messages) {
            ChatMessage langchain4jMsg = convertMessage(msg);
            result.add(langchain4jMsg);
        }
        
        return result;
    }
    
    private ChatMessage convertMessage(MultimodalMessage message) {
        String role = message.getRole();
        
        if ("system".equals(role)) {
            return new SystemMessage(message.getTextContent());
        }
        
        if ("assistant".equals(role)) {
            return new AiMessage(message.getTextContent());
        }
        
        List<Content> contents = new ArrayList<>();
        
        for (MediaContent content : message.getContents()) {
            switch (content.getType()) {
                case TEXT:
                    if (content.getText() != null && !content.getText().isEmpty()) {
                        contents.add(new TextContent(content.getText()));
                    }
                    break;
                    
                case IMAGE:
                    if (content.getUrl() != null) {
                        contents.add(new ImageContent(content.getUrl()));
                    } else if (content.getData() != null) {
                        String mimeType = content.getMediaType() != null ? content.getMediaType() : "image/png";
                        contents.add(new ImageContent(content.getData(), mimeType));
                    }
                    break;
                    
                case AUDIO:
                    if (content.getUrl() != null) {
                        contents.add(new AudioContent(content.getUrl()));
                    } else if (content.getData() != null) {
                        String mimeType = content.getMediaType() != null ? content.getMediaType() : "audio/mp3";
                        contents.add(new AudioContent(content.getData(), mimeType));
                    }
                    break;
                    
                default:
                    log.warn("Unsupported content type: {}", content.getType());
            }
        }
        
        if (contents.size() == 1 && contents.get(0) instanceof TextContent) {
            return new UserMessage(((TextContent) contents.get(0)).text());
        }
        
        return UserMessage.from(contents);
    }
}
