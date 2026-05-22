package com.spintale.ai.runtime.prompt;

import com.spintale.ai.core.model.ChatMessage;
import java.util.ArrayList;
import java.util.List;

/**
 * Message builder for constructing chat messages.
 */
public class MessageBuilder {

    private final List<ChatMessage> messages = new ArrayList<>();

    /**
     * Add a system message.
     */
    public MessageBuilder system(String content) {
        messages.add(ChatMessage.system(content));
        return this;
    }

    /**
     * Add a user message.
     */
    public MessageBuilder user(String content) {
        messages.add(ChatMessage.user(content));
        return this;
    }

    /**
     * Add an assistant message.
     */
    public MessageBuilder assistant(String content) {
        messages.add(ChatMessage.assistant(content));
        return this;
    }

    /**
     * Build the message list.
     */
    public List<ChatMessage> build() {
        return new ArrayList<>(messages);
    }

    /**
     * Create a new message builder.
     */
    public static MessageBuilder create() {
        return new MessageBuilder();
    }
}
