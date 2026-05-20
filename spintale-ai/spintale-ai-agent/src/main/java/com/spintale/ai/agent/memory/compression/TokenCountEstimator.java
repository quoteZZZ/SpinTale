package com.spintale.ai.agent.memory.compression;

import com.spintale.ai.core.model.ChatMessage;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TokenCountEstimator {
    
    private static final double CHARS_PER_TOKEN_ENGLISH = 4.0;
    private static final double CHARS_PER_TOKEN_CHINESE = 1.5;
    
    public int estimateTokenCount(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        
        int chineseChars = 0;
        int totalChars = text.length();
        
        for (int i = 0; i < totalChars; i++) {
            char c = text.charAt(i);
            if (Character.toString(c).matches("[\\u4e00-\\u9fa5]")) {
                chineseChars++;
            }
        }
        
        int nonChineseChars = totalChars - chineseChars;
        
        int chineseTokens = (int) Math.ceil(chineseChars / CHARS_PER_TOKEN_CHINESE);
        int englishTokens = (int) Math.ceil(nonChineseChars / CHARS_PER_TOKEN_ENGLISH);
        
        return chineseTokens + englishTokens + 4;
    }
    
    public int estimateTokenCount(ChatMessage message) {
        int count = 0;
        count += estimateTokenCount(message.getRole());
        count += estimateTokenCount(message.getContent());
        return count;
    }
    
    public int estimateTokenCount(List<ChatMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return 0;
        }
        
        int total = 0;
        for (ChatMessage msg : messages) {
            total += estimateTokenCount(msg);
        }
        return total;
    }
    
    public int findOptimalSplitPoint(List<ChatMessage> messages, int targetTokenCount) {
        int accumulated = 0;
        for (int i = 0; i < messages.size(); i++) {
            accumulated += estimateTokenCount(messages.get(i));
            if (accumulated >= targetTokenCount) {
                return i;
            }
        }
        return messages.size();
    }
}
