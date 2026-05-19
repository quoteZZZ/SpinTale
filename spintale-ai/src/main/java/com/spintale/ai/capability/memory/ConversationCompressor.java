package com.spintale.ai.capability.memory;

import com.spintale.ai.core.model.ChatMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Compresses older conversation turns into a compact summary while preserving
 * recent turns verbatim.
 */
@Slf4j
@Service
public class ConversationCompressor {

    private static final int DEFAULT_WINDOW_SIZE = 10;
    private static final int MAX_SUMMARY_LENGTH = 500;

    public List<ChatMessage> compress(List<ChatMessage> messages) {
        return compress(messages, DEFAULT_WINDOW_SIZE);
    }

    public List<ChatMessage> compress(List<ChatMessage> messages, int windowSize) {
        if (messages == null || messages.isEmpty()) {
            return List.of();
        }
        int safeWindowSize = Math.max(1, windowSize);
        if (messages.size() <= safeWindowSize) {
            return List.copyOf(messages);
        }

        int compressCount = messages.size() - safeWindowSize;
        List<ChatMessage> olderMessages = messages.subList(0, compressCount);
        List<ChatMessage> recentMessages = messages.subList(compressCount, messages.size());

        List<ChatMessage> compressed = new ArrayList<>();
        compressed.add(ChatMessage.system("[Summary] " + summarize(olderMessages)));
        compressed.addAll(recentMessages);

        log.debug("Compressed conversation history: original={}, compressed={}", messages.size(), compressed.size());
        return compressed;
    }

    private String summarize(List<ChatMessage> messages) {
        StringBuilder summary = new StringBuilder();
        int captured = 0;
        for (ChatMessage message : messages) {
            if (captured >= 5) {
                break;
            }
            if (message == null || message.getContent() == null || message.getContent().isBlank()) {
                continue;
            }
            summary.append(message.getRole() == null ? "message" : message.getRole())
                    .append(": ")
                    .append(truncate(message.getContent(), 80))
                    .append("; ");
            captured++;
        }
        if (summary.length() > MAX_SUMMARY_LENGTH) {
            summary.setLength(MAX_SUMMARY_LENGTH);
            summary.append("...");
        }
        return summary.toString();
    }

    private String truncate(String text, int maxLength) {
        return text.length() <= maxLength ? text : text.substring(0, maxLength) + "...";
    }
}
