package com.spintale.ai.core.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import com.spintale.ai.core.model.ChatMessage;
import com.spintale.ai.core.model.ChatRequest;
import com.spintale.ai.core.model.ChatResponse;
import com.spintale.ai.core.service.AiChatService;

import reactor.core.publisher.Flux;

/**
 * Fluent client facade for application code and declarative AI service proxies.
 */
public class DefaultChatClient implements ChatClient {

    private final AiChatService chatService;

    public DefaultChatClient(AiChatService chatService) {
        this.chatService = chatService;
    }

    @Override
    public PromptBuilder prompt() {
        return new DefaultPromptBuilder(chatService);
    }

    @Override
    public String call(String message) {
        return chatService.chat(message);
    }

    @Override
    public Flux<String> stream(String message) {
        return prompt().user(message).stream().content();
    }

    private static class DefaultPromptBuilder implements PromptBuilder {

        private final AiChatService chatService;
        private final Map<String, Object> extraParams = new HashMap<>();
        private String userMessage;
        private String systemPrompt;
        private List<ChatMessage> messages;
        private Double temperature;
        private Integer maxTokens;
        private String sessionId;
        private String userId;

        DefaultPromptBuilder(AiChatService chatService) {
            this.chatService = chatService;
        }

        @Override
        public PromptBuilder user(String message) {
            this.userMessage = message;
            return this;
        }

        @Override
        public PromptBuilder system(String systemPrompt) {
            this.systemPrompt = systemPrompt;
            return this;
        }

        @Override
        public PromptBuilder messages(List<ChatMessage> messages) {
            this.messages = messages;
            return this;
        }

        @Override
        public PromptBuilder temperature(double temperature) {
            this.temperature = temperature;
            return this;
        }

        @Override
        public PromptBuilder maxTokens(int maxTokens) {
            this.maxTokens = maxTokens;
            return this;
        }

        @Override
        public PromptBuilder sessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        @Override
        public PromptBuilder userId(String userId) {
            this.userId = userId;
            return this;
        }

        @Override
        public PromptBuilder provider(String providerId) {
            return param("provider", providerId);
        }

        @Override
        public PromptBuilder param(String name, Object value) {
            if (name != null && !name.isBlank() && value != null) {
                this.extraParams.put(name, value);
            }
            return this;
        }

        @Override
        public PromptBuilder tools(String... toolNames) {
            this.extraParams.put("tools", toolNames == null ? List.of() : List.of(toolNames));
            return this;
        }

        @Override
        public CallSpec call() {
            ChatRequest request = buildRequest(false);
            return () -> chatService.chat(request);
        }

        @Override
        public StreamSpec stream() {
            ChatRequest request = buildRequest(true);
            return () -> Flux.create(sink -> chatService.streamChat(request, new AiChatService.StreamHandler() {
                private final AtomicBoolean tokenEmitted = new AtomicBoolean(false);

                @Override
                public void onToken(String token) {
                    if (token == null || token.isEmpty()) {
                        return;
                    }
                    tokenEmitted.set(true);
                    sink.next(ChatResponse.builder()
                            .sessionId(request.getSessionId())
                            .content(token)
                            .finished(false)
                            .build());
                }

                @Override
                public void onComplete(ChatResponse response) {
                    if (tokenEmitted.get()) {
                        sink.next(ChatResponse.builder()
                                .sessionId(response != null ? response.getSessionId() : request.getSessionId())
                                .content("")
                                .model(response != null ? response.getModel() : null)
                                .tokenUsage(response != null ? response.getTokenUsage() : null)
                                .toolCalls(response != null ? response.getToolCalls() : null)
                                .finished(true)
                                .finishReason(response != null ? response.getFinishReason() : null)
                                .extraData(response != null ? response.getExtraData() : null)
                                .requiresToolExecution(response != null ? response.getRequiresToolExecution() : false)
                                .reasoningTrace(response != null ? response.getReasoningTrace() : null)
                                .build());
                    } else if (response != null) {
                        sink.next(response);
                    }
                    sink.complete();
                }

                @Override
                public void onError(Throwable error) {
                    sink.error(error);
                }
            }));
        }

        private ChatRequest buildRequest(boolean stream) {
            return ChatRequest.builder()
                    .message(userMessage)
                    .systemPrompt(systemPrompt)
                    .messages(messages)
                    .sessionId(sessionId)
                    .userId(userId)
                    .temperature(temperature)
                    .maxTokens(maxTokens)
                    .stream(stream)
                    .extraParams(extraParams.isEmpty() ? null : Map.copyOf(extraParams))
                    .build();
        }
    }
}
