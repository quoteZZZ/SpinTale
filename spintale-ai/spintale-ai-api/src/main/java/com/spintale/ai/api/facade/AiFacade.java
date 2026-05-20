package com.spintale.ai.api.facade;

import com.spintale.ai.agent.react.api.AgentResult;
import com.spintale.ai.agent.react.api.AgentService;
import com.spintale.ai.api.chat.ChatClient;
import com.spintale.ai.core.spi.ChatModel;
import com.spintale.ai.core.spi.StreamingChatModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * AI功能统一门面
 * 
 * 提供简化的API，屏蔽底层复杂性：
 * - 简单对话
 * - RAG对话
 * - Agent对话
 * - 流式对话
 * 
 * 使用示例：
 * <pre>
 * AiFacade ai = new AiFacade(chatModel, agentService);
 * 
 * // 简单对话
 * String answer = ai.chat("What is Java?");
 * 
 * // RAG对话
 * String answer = ai.chatWithRag("Explain this document", "docs-collection");
 * 
 * // Agent对话
 * String answer = ai.chatWithAgent("Search for weather in Beijing", tools);
 * 
 * // 流式对话
 * ai.chatStreaming("Tell me a story", token -> System.out.print(token));
 * </pre>
 */
public class AiFacade {

    private static final Logger log = LoggerFactory.getLogger(AiFacade.class);

    private final ChatModel chatModel;
    private final StreamingChatModel streamingModel;
    private final AgentService agentService;
    private final ChatClient chatClient;

    public AiFacade(ChatModel chatModel, AgentService agentService) {
        this(chatModel, null, agentService);
    }

    public AiFacade(ChatModel chatModel, StreamingChatModel streamingModel, AgentService agentService) {
        this.chatModel = chatModel;
        this.streamingModel = streamingModel;
        this.agentService = agentService;
        this.chatClient = ChatClient.builder(chatModel).build();
    }

    /**
     * 简单对话
     */
    public String chat(String message) {
        log.debug("Simple chat: {}", truncate(message, 100));
        return chatClient.user(message).call().content();
    }

    /**
     * 带系统提示的对话
     */
    public String chat(String systemMessage, String userMessage) {
        log.debug("Chat with system prompt");
        return chatClient
                .system(systemMessage)
                .user(userMessage)
                .call()
                .content();
    }

    /**
     * 带上下文的对话
     */
    public String chat(String message, List<Map<String, String>> context) {
        log.debug("Chat with context, history size: {}", context.size());
        ChatClient client = ChatClient.builder(chatModel).build();
        
        for (Map<String, String> msg : context) {
            String role = msg.get("role");
            String content = msg.get("content");
            if ("user".equals(role)) {
                client.user(content);
            } else if ("assistant".equals(role)) {
                client.assistant(content);
            }
        }
        
        return client.user(message).call().content();
    }

    /**
     * RAG对话（简化版）
     */
    public String chatWithRag(String message, String collection) {
        log.info("RAG chat: collection={}, query={}", collection, truncate(message, 100));
        
        if (agentService != null) {
            AgentResult result = agentService.execute(message);
            return result.isSuccess() ? result.getContent() : "Error: " + result.getContent();
        }
        
        return chat("Use the following context to answer the question.\nCollection: " + collection, message);
    }

    /**
     * Agent对话（带工具调用）
     */
    public String chatWithAgent(String message, List<String> tools) {
        log.info("Agent chat: tools={}, task={}", tools, truncate(message, 100));
        
        if (agentService == null) {
            log.warn("AgentService not available, falling back to simple chat");
            return chat(message);
        }
        
        AgentResult result = agentService.executeWithTools(message, tools);
        return result.isSuccess() ? result.getContent() : "Error: " + result.getContent();
    }

    /**
     * 流式对话
     */
    public void chatStreaming(String message, Consumer<String> tokenConsumer) {
        log.debug("Streaming chat: {}", truncate(message, 100));
        
        if (streamingModel != null) {
            StringBuilder fullResponse = new StringBuilder();
            streamingModel.chat(
                    List.of(new dev.langchain4j.data.message.UserMessage(message)),
                    new dev.langchain4j.model.chat.response.StreamingChatResponseHandler() {
                        @Override
                        public void onPartialResponse(String token) {
                            fullResponse.append(token);
                            tokenConsumer.accept(token);
                        }

                        @Override
                        public void onCompleteResponse(dev.langchain4j.model.chat.response.ChatResponse response) {
                            log.debug("Streaming completed, total length: {}", fullResponse.length());
                        }

                        @Override
                        public void onError(Throwable error) {
                            log.error("Streaming error", error);
                            tokenConsumer.accept("\n[Error: " + error.getMessage() + "]");
                        }
                    }
            );
        } else {
            String response = chat(message);
            tokenConsumer.accept(response);
        }
    }

    /**
     * 带最大迭代的Agent对话
     */
    public String chatWithAgent(String message, int maxIterations) {
        log.info("Agent chat with max iterations: {}", maxIterations);
        
        if (agentService == null) {
            return chat(message);
        }
        
        AgentResult result = agentService.execute(message, maxIterations);
        return result.isSuccess() ? result.getContent() : "Error: " + result.getContent();
    }

    /**
     * 获取对话统计信息
     */
    public Map<String, Object> getStats() {
        return Map.of(
                "hasChatModel", chatModel != null,
                "hasStreamingModel", streamingModel != null,
                "hasAgentService", agentService != null
        );
    }

    private String truncate(String s, int maxLen) {
        if (s == null) return "";
        return s.length() <= maxLen ? s : s.substring(0, maxLen) + "...";
    }
}
