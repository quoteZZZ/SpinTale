package com.spintale.ai.capability.memory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 记忆压缩服务
 * 使用滑动窗口摘要算法压缩长对话历史，减少 Token 消耗
 */
@Slf4j
@Service
public class MemoryCompressionService {

    private static final int DEFAULT_WINDOW_SIZE = 10; // 默认窗口大小（对话轮数）
    private static final int MAX_COMPRESSED_LENGTH = 500; // 最大压缩后长度（字符数）

    /**
     * 压缩对话历史
     * @param messages 原始消息列表
     * @return 压缩后的消息列表
     */
    public List<CompressedMessage> compress(List<ConversationMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return new ArrayList<>();
        }

        log.info("开始压缩对话历史：原始消息数={}", messages.size());

        List<CompressedMessage> compressed = new ArrayList<>();

        if (messages.size() <= DEFAULT_WINDOW_SIZE) {
            // 消息较少，无需压缩
            for (ConversationMessage msg : messages) {
                compressed.add(new CompressedMessage(msg.role(), msg.content(), false));
            }
        } else {
            // 使用滑动窗口：保留最近 N 条，压缩前面的
            int compressCount = messages.size() - DEFAULT_WINDOW_SIZE;
            
            // 压缩早期消息
            List<ConversationMessage> toCompress = messages.subList(0, compressCount);
            String summary = generateSummary(toCompress);
            compressed.add(new CompressedMessage("system", "[摘要] " + summary, true));
            
            // 保留最近的原始消息
            List<ConversationMessage> recent = messages.subList(compressCount, messages.size());
            for (ConversationMessage msg : recent) {
                compressed.add(new CompressedMessage(msg.role(), msg.content(), false));
            }
        }

        log.info("压缩完成：压缩后消息数={}", compressed.size());
        return compressed;
    }

    /**
     * 生成消息摘要
     * 实际使用时可调用 LLM 进行智能摘要
     */
    private String generateSummary(List<ConversationMessage> messages) {
        StringBuilder summary = new StringBuilder();
        
        // 简单实现：提取关键信息（实际应调用 LLM）
        for (int i = 0; i < Math.min(messages.size(), 5); i++) {
            ConversationMessage msg = messages.get(i);
            if (msg.role().equals("user")) {
                summary.append("用户询问：").append(truncate(msg.content(), 50)).append("; ");
            }
        }
        
        if (summary.length() > MAX_COMPRESSED_LENGTH) {
            summary.setLength(MAX_COMPRESSED_LENGTH);
            summary.append("...");
        }
        
        return summary.toString();
    }

    /**
     * 截断文本
     */
    private String truncate(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "...";
    }

    /**
     * 对话消息记录
     */
    public record ConversationMessage(String role, String content) {}

    /**
     * 压缩后的消息记录
     */
    public record CompressedMessage(String role, String content, boolean isSummary) {}
}
