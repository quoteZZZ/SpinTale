package com.spintale.ai.agent.memory.advisor;

import java.util.ArrayList;
import java.util.List;

import com.spintale.ai.agent.memory.ConversationMessage;
import com.spintale.ai.agent.memory.ConversationSession;
import com.spintale.ai.agent.memory.LongTermMemory;
import com.spintale.ai.agent.memory.api.ConversationCompressor;
import com.spintale.ai.agent.memory.api.ConversationManager;
import com.spintale.ai.agent.memory.api.LongTermMemoryManager;
import com.spintale.ai.api.advisor.Advisor;
import com.spintale.ai.api.advisor.AdvisorContext;
import com.spintale.ai.api.advisor.AdvisorOrder;
import com.spintale.ai.api.advisor.AdvisorRequest;
import com.spintale.ai.api.advisor.AdvisorResponse;
import com.spintale.ai.core.model.ChatMessage;

public class MemoryAdvisor implements Advisor {

    private final ConversationManager conversationManager;
    private final LongTermMemoryManager longTermMemoryManager;
    private final ConversationCompressor conversationCompressor;
    private int maxContextMessages = 20;
    private double memoryRetrievalThreshold = 0.6;
    private int maxRetrievedMemories = 5;

    public MemoryAdvisor(ConversationManager conversationManager,
                         LongTermMemoryManager longTermMemoryManager,
                         ConversationCompressor conversationCompressor) {
        this.conversationManager = conversationManager;
        this.longTermMemoryManager = longTermMemoryManager;
        this.conversationCompressor = conversationCompressor;
    }

    @Override
    public String getName() {
        return "MemoryAdvisor";
    }

    @Override
    public int getOrder() {
        return AdvisorOrder.MEMORY;
    }

    @Override
    public AdvisorRequest adviseRequest(AdvisorRequest request, AdvisorContext context) {
        if (request == null || conversationManager == null) {
            return request;
        }

        ConversationSession session = resolveSession(request);
        if (session != null) {
            request.setSessionId(session.getSessionId());
            request.setHistory(buildHistory(session));
        }

        List<LongTermMemory> memories = retrieveMemories(request.getUserId(), request.getUserMessage());
        context.put(AdvisorContext.RETRIEVED_MEMORIES, memories);
        if (!memories.isEmpty()) {
            request.setSystemPrompt(appendMemories(request.getSystemPrompt(), memories));
        }
        return request;
    }

    @Override
    public AdvisorResponse adviseResponse(AdvisorResponse response, AdvisorContext context) {
        if (response != null && response.getSessionId() != null && conversationManager != null) {
            Object originalQuery = context.getAttribute(AdvisorContext.ORIGINAL_QUERY);
            if (originalQuery instanceof String query) {
                conversationManager.addMessage(response.getSessionId(), "user", query);
            }
            conversationManager.addMessage(response.getSessionId(), "assistant", response.getContent());
        }
        return response;
    }

    private ConversationSession resolveSession(AdvisorRequest request) {
        if (request.getSessionId() != null) {
            ConversationSession existing = conversationManager.getSession(request.getSessionId());
            if (existing != null) {
                return existing;
            }
        }
        return conversationManager.createSession(request.getUserId() == null ? "anonymous" : request.getUserId());
    }

    private List<ChatMessage> buildHistory(ConversationSession session) {
        List<ChatMessage> history = new ArrayList<>();
        for (ConversationMessage message : session.getRecentMessages(maxContextMessages)) {
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setRole(message.getRole());
            chatMessage.setContent(message.getContent());
            history.add(chatMessage);
        }
        return conversationCompressor == null ? history : conversationCompressor.compress(history, maxContextMessages);
    }

    private List<LongTermMemory> retrieveMemories(String userId, String message) {
        if (longTermMemoryManager == null || userId == null || message == null) {
            return List.of();
        }
        return longTermMemoryManager.searchMemories(
                userId, message, maxRetrievedMemories, memoryRetrievalThreshold);
    }

    private String appendMemories(String systemPrompt, List<LongTermMemory> memories) {
        StringBuilder builder = new StringBuilder(systemPrompt == null ? "" : systemPrompt);
        builder.append("\n\nUser memory:\n");
        for (LongTermMemory memory : memories) {
            builder.append("- ").append(memory.getContent()).append('\n');
        }
        return builder.toString();
    }

    public MemoryAdvisor setMaxContextMessages(int maxContextMessages) {
        this.maxContextMessages = Math.max(1, maxContextMessages);
        return this;
    }

    public MemoryAdvisor setMemoryRetrievalThreshold(double threshold) {
        this.memoryRetrievalThreshold = threshold;
        return this;
    }

    public MemoryAdvisor setMaxRetrievedMemories(int max) {
        this.maxRetrievedMemories = Math.max(1, max);
        return this;
    }
}
