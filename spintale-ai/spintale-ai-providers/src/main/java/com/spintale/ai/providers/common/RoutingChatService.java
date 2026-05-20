package com.spintale.ai.providers.common;

import java.util.List;

import com.spintale.ai.core.model.ChatRequest;
import com.spintale.ai.core.model.ChatResponse;
import com.spintale.ai.api.pipeline.AiChatInterceptor;
import com.spintale.ai.core.provider.AiModelProvider;
import com.spintale.ai.core.provider.AiProviderRegistry;
import com.spintale.ai.core.service.AiChatService;

/**
 * Default AI gateway. It selects a provider, then applies the custom pipeline.
 */
public class RoutingChatService implements AiChatService {

    private final AiProviderRegistry providerRegistry;
    private final List<AiChatInterceptor> interceptors;

    public RoutingChatService(AiProviderRegistry providerRegistry,
                                        List<AiChatInterceptor> interceptors) {
        this.providerRegistry = providerRegistry;
        this.interceptors = interceptors == null ? List.of() : List.copyOf(interceptors);
    }

    @Override
    public String chat(String message) {
        return chat(ChatRequest.builder().message(message).build()).getContent();
    }

    @Override
    public ChatResponse chat(ChatRequest request) {
        AiModelProvider provider = providerRegistry.select(null);
        AiChatService service = provider.getChatService();
        
        ChatRequest processedRequest = request;
        for (AiChatInterceptor interceptor : interceptors) {
            processedRequest = interceptor.beforeChat(processedRequest);
        }
        
        ChatResponse response = service.chat(processedRequest);
        
        ChatResponse processedResponse = response;
        for (int i = interceptors.size() - 1; i >= 0; i--) {
            processedResponse = interceptors.get(i).afterChat(processedRequest, processedResponse);
        }
        
        return processedResponse;
    }

    @Override
    public void streamChat(ChatRequest request, StreamHandler handler) {
        AiModelProvider provider = providerRegistry.select(null);
        AiChatService service = provider.getChatService();
        service.streamChat(request, handler);
    }
}
