package com.spintale.ai.infrastructure.provider;

import java.util.List;

import com.spintale.ai.core.model.ChatRequest;
import com.spintale.ai.core.model.ChatResponse;
import com.spintale.ai.core.pipeline.AiChatInterceptor;
import com.spintale.ai.core.pipeline.ChatPipelineService;
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
        AiChatService service = selectService(request);
        return service.chat(request);
    }

    @Override
    public void streamChat(ChatRequest request, StreamHandler handler) {
        AiChatService service = selectService(request);
        service.streamChat(request, handler);
    }

    private AiChatService selectService(ChatRequest request) {
        AiModelProvider provider = providerRegistry.select(request);
        return new ChatPipelineService(provider.getChatService(), interceptors);
    }
}
