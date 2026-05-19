package com.spintale.ai.core.pipeline;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.spintale.ai.core.model.ChatRequest;
import com.spintale.ai.core.model.ChatResponse;
import com.spintale.ai.core.service.AiChatService;

/**
 * Decorates an AiChatService with ordered request/response interceptors.
 */
public class ChatPipelineService implements AiChatService {

    private final AiChatService delegate;
    private final List<AiChatInterceptor> interceptors;

    public ChatPipelineService(AiChatService delegate, List<AiChatInterceptor> interceptors) {
        this.delegate = delegate;
        this.interceptors = new ArrayList<>(interceptors == null ? List.of() : interceptors);
        this.interceptors.sort(Comparator.comparingInt(AiChatInterceptor::getOrder));
    }

    @Override
    public String chat(String message) {
        return chat(ChatRequest.builder().message(message).build()).getContent();
    }

    @Override
    public ChatResponse chat(ChatRequest request) {
        ChatRequest currentRequest = request == null ? ChatRequest.builder().build() : request;
        try {
            currentRequest = applyBefore(currentRequest);
            ChatResponse shortCircuit = getShortCircuitResponse(currentRequest);
            if (shortCircuit != null) {
                return applyAfter(currentRequest, shortCircuit);
            }
            ChatResponse response = delegate.chat(currentRequest);
            return applyAfter(currentRequest, response);
        } catch (RuntimeException e) {
            notifyError(currentRequest, e);
            throw e;
        } catch (Exception e) {
            notifyError(currentRequest, e);
            throw new IllegalStateException("AI chat pipeline failed", e);
        }
    }

    @Override
    public void streamChat(ChatRequest request, StreamHandler handler) {
        ChatRequest currentRequest = request == null ? ChatRequest.builder().build() : request;
        try {
            currentRequest = applyBefore(currentRequest);
            ChatResponse shortCircuit = getShortCircuitResponse(currentRequest);
            if (shortCircuit != null) {
                handler.onComplete(applyAfter(currentRequest, shortCircuit));
                return;
            }

            ChatRequest pipelineRequest = currentRequest;
            delegate.streamChat(pipelineRequest, new StreamHandler() {
                @Override
                public void onToken(String token) {
                    handler.onToken(token);
                }

                @Override
                public void onComplete(ChatResponse response) {
                    handler.onComplete(applyAfter(pipelineRequest, response));
                }

                @Override
                public void onError(Throwable error) {
                    notifyError(pipelineRequest, error);
                    handler.onError(error);
                }
            });
        } catch (RuntimeException e) {
            notifyError(currentRequest, e);
            handler.onError(e);
        } catch (Exception e) {
            notifyError(currentRequest, e);
            handler.onError(new IllegalStateException("AI chat pipeline failed", e));
        }
    }

    private ChatRequest applyBefore(ChatRequest request) {
        ChatRequest currentRequest = request == null ? ChatRequest.builder().build() : request;
        for (AiChatInterceptor interceptor : interceptors) {
            currentRequest = interceptor.beforeChat(currentRequest);
        }
        return currentRequest;
    }

    private ChatResponse applyAfter(ChatRequest request, ChatResponse response) {
        ChatResponse currentResponse = response;
        for (int i = interceptors.size() - 1; i >= 0; i--) {
            currentResponse = interceptors.get(i).afterChat(request, currentResponse);
        }
        return currentResponse;
    }

    private void notifyError(ChatRequest request, Throwable error) {
        for (AiChatInterceptor interceptor : interceptors) {
            interceptor.onError(request, error);
        }
    }

    private ChatResponse getShortCircuitResponse(ChatRequest request) {
        if (request == null || request.getExtraParams() == null) {
            return null;
        }
        Object value = request.getExtraParams().get(PipelineAttributes.SHORT_CIRCUIT_RESPONSE);
        if (value instanceof ChatResponse response) {
            return response;
        }
        if (value instanceof String content) {
            return ChatResponse.builder()
                    .sessionId(request.getSessionId())
                    .content(content)
                    .model("cache")
                    .finished(true)
                    .build();
        }
        return null;
    }
}
