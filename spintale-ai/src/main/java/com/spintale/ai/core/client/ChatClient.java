package com.spintale.ai.core.client;

import com.spintale.ai.core.model.ChatRequest;
import com.spintale.ai.core.model.ChatResponse;
import reactor.core.publisher.Flux;

/**
 * Fluent AI chat client for application code and declarative service proxies.
 */
public interface ChatClient {

    PromptBuilder prompt();

    String call(String message);

    Flux<String> stream(String message);

    interface PromptBuilder {

        PromptBuilder user(String message);

        PromptBuilder system(String systemPrompt);

        PromptBuilder messages(java.util.List<com.spintale.ai.core.model.ChatMessage> messages);

        PromptBuilder temperature(double temperature);

        PromptBuilder maxTokens(int maxTokens);

        PromptBuilder sessionId(String sessionId);

        PromptBuilder userId(String userId);

        PromptBuilder provider(String providerId);

        PromptBuilder param(String name, Object value);

        PromptBuilder tools(String... toolNames);

        CallSpec call();

        StreamSpec stream();
    }

    interface CallSpec {

        ChatResponse execute();

        default String content() {
            return execute().getContent();
        }
    }

    interface StreamSpec {

        Flux<ChatResponse> execute();

        default Flux<String> content() {
            return execute()
                    .map(ChatResponse::getContent)
                    .filter(content -> content != null && !content.isEmpty());
        }
    }
}
