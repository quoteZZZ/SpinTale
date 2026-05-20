package com.spintale.ai.agent.memory.compression;

import com.spintale.ai.core.model.ChatMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class LlmConversationCompressor implements ConversationCompressor {
    
    private static final Logger log = LoggerFactory.getLogger(LlmConversationCompressor.class);
    
    private static final String SUMMARY_PROMPT = """
            请将以下对话历史压缩成简洁的摘要，保留关键信息和重要决策。
            
            要求：
            1. 保留用户的核心需求和意图
            2. 保留AI提供的关键答案和解决方案
            3. 保留重要的上下文信息（如用户偏好、约束条件等）
            4. 删除重复、无关或琐碎的内容
            5. 使用简洁的自然语言表达
            6. 摘要长度不超过200字
            
            对话历史：
            %s
            
            请输出摘要：
            """;
    
    private final ChatModel chatModel;
    private final TokenCountEstimator tokenEstimator;
    
    @Value("${spintale.ai.memory.compression.enabled:true}")
    private boolean compressionEnabled = true;
    
    @Value("${spintale.ai.memory.compression.max-tokens:4000}")
    private int maxTokens = 4000;
    
    @Value("${spintale.ai.memory.compression.summary-threshold:6}")
    private int summaryThreshold = 6;
    
    @Autowired
    public LlmConversationCompressor(ChatModel chatModel, TokenCountEstimator tokenEstimator) {
        this.chatModel = chatModel;
        this.tokenEstimator = tokenEstimator;
    }
    
    @Override
    public List<ChatMessage> compress(List<ChatMessage> messages, int targetTokenCount) {
        if (!compressionEnabled || messages == null || messages.isEmpty()) {
            return messages;
        }
        
        int currentTokens = estimateTokenCount(messages);
        
        if (currentTokens <= targetTokenCount) {
            return messages;
        }
        
        log.info("Compressing conversation: currentTokens={}, targetTokens={}", currentTokens, targetTokenCount);
        
        if (messages.size() <= summaryThreshold) {
            return messages;
        }
        
        List<ChatMessage> recentMessages = new ArrayList<>();
        int recentTokenCount = 0;
        
        for (int i = messages.size() - 1; i >= 0; i--) {
            ChatMessage msg = messages.get(i);
            int msgTokens = tokenEstimator.estimateTokenCount(msg);
            
            if (recentTokenCount + msgTokens > targetTokenCount * 0.6) {
                break;
            }
            
            recentMessages.add(0, msg);
            recentTokenCount += msgTokens;
        }
        
        int toSummarizeCount = messages.size() - recentMessages.size();
        if (toSummarizeCount >= 2) {
            List<ChatMessage> toSummarize = messages.subList(0, toSummarizeCount);
            List<ChatMessage> summary = summarize(toSummarize);
            
            List<ChatMessage> compressed = new ArrayList<>();
            compressed.addAll(summary);
            compressed.addAll(recentMessages);
            
            log.info("Conversation compressed: originalSize={}, compressedSize={}, savedTokens={}",
                    messages.size(), compressed.size(), currentTokens - estimateTokenCount(compressed));
            
            return compressed;
        }
        
        return messages;
    }
    
    @Override
    public List<ChatMessage> summarize(List<ChatMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return messages;
        }
        
        try {
            String conversationText = formatConversation(messages);
            String prompt = String.format(SUMMARY_PROMPT, conversationText);
            
            String summary = chatModel.generate(prompt);
            
            ChatMessage summaryMessage = new ChatMessage();
            summaryMessage.setRole("system");
            summaryMessage.setContent("[对话摘要] " + summary.trim());
            
            log.debug("Generated summary: {}", summary.trim());
            
            return List.of(summaryMessage);
        } catch (Exception e) {
            log.error("Failed to summarize conversation", e);
            return messages;
        }
    }
    
    @Override
    public int estimateTokenCount(List<ChatMessage> messages) {
        return tokenEstimator.estimateTokenCount(messages);
    }
    
    private String formatConversation(List<ChatMessage> messages) {
        return messages.stream()
                .map(msg -> String.format("[%s]: %s", msg.getRole(), msg.getContent()))
                .collect(Collectors.joining("\n\n"));
    }
    
    public LlmConversationCompressor setCompressionEnabled(boolean enabled) {
        this.compressionEnabled = enabled;
        return this;
    }
    
    public LlmConversationCompressor setMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
        return this;
    }
}
