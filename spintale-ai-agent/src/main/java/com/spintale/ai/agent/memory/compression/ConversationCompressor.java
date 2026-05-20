package com.spintale.ai.agent.memory.compression;

import com.spintale.ai.core.model.ChatMessage;

import java.util.List;

public interface ConversationCompressor {
    
    List<ChatMessage> compress(List<ChatMessage> messages, int targetTokenCount);
    
    List<ChatMessage> summarize(List<ChatMessage> messages);
    
    int estimateTokenCount(List<ChatMessage> messages);
}
