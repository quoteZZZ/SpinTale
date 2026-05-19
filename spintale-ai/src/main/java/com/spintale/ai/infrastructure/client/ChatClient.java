package com.spintale.ai.infrastructure.client;

import com.spintale.ai.core.model.ChatRequest;
import com.spintale.ai.core.model.ChatResponse;
import reactor.core.publisher.Flux;

/**
 * 统一的 AI Chat 客户端接口
 * 
 * 参考 Spring AI 的 ChatClient 设计，提供：
 * - 流畅的API风格
 * - 同步/异步/流式调用
 * - 自动重试和熔断
 * - 统一的错误处理
 * 
 * 使用示例：
 * <pre>{@code
 * ChatClient client = chatClientBuilder.build();
 * 
 * // 同步调用
 * String response = client.prompt()
 *     .user("你好")
 *     .system("你是一个助手")
 *     .call()
 *     .content();
 * 
 * // 流式调用
 * Flux<String> stream = client.prompt()
 *     .user("讲个故事")
 *     .stream()
 *     .content();
 * }</pre>
 */
public interface ChatClient {

    /**
     * 创建 Prompt 构建器
     */
    PromptBuilder prompt();

    /**
     * 直接调用（简单场景）
     */
    String call(String message);

    /**
     * 流式调用（简单场景）
     */
    Flux<String> stream(String message);

    /**
     * Prompt 构建器
     */
    interface PromptBuilder {
        
        /**
         * 设置用户消息
         */
        PromptBuilder user(String message);
        
        /**
         * 设置系统提示词
         */
        PromptBuilder system(String systemPrompt);
        
        /**
         * 添加历史消息
         */
        PromptBuilder messages(java.util.List<com.spintale.ai.core.model.ChatMessage> messages);
        
        /**
         * 设置温度参数
         */
        PromptBuilder temperature(double temperature);
        
        /**
         * 设置最大Token数
         */
        PromptBuilder maxTokens(int maxTokens);
        
        /**
         * 设置会话ID
         */
        PromptBuilder sessionId(String sessionId);
        
        /**
         * 启用工具调用
         */
        PromptBuilder tools(String... toolNames);
        
        /**
         * 获取同步调用器
         */
        CallSpec call();
        
        /**
         * 获取流式调用器
         */
        StreamSpec stream();
    }

    /**
     * 同步调用规范
     */
    interface CallSpec {
        
        /**
         * 执行调用
         */
        ChatResponse execute();
        
        /**
         * 获取内容
         */
        default String content() {
            return execute().getContent();
        }
    }

    /**
     * 流式调用规范
     */
    interface StreamSpec {
        
        /**
         * 执行流式调用
         */
        Flux<ChatResponse> execute();
        
        /**
         * 获取内容流
         */
        default Flux<String> content() {
            return execute().map(ChatResponse::getContent);
        }
    }
}
