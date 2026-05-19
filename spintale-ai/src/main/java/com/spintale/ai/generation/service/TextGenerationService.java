package com.spintale.ai.generation.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.spintale.ai.core.model.ChatRequest;
import com.spintale.ai.core.model.ChatResponse;
import com.spintale.ai.core.service.AiChatService;

/**
 * Text generation facade backed by the unified AI gateway.
 */
public class TextGenerationService {

    private static final Logger log = LoggerFactory.getLogger(TextGenerationService.class);

    private final AiChatService aiChatService;

    public TextGenerationService(AiChatService aiChatService) {
        this.aiChatService = aiChatService;
    }

    public String generate(String action, String prompt) {
        return generate(action, prompt, null);
    }

    public String generate(String action, String prompt, String systemPrompt) {
        String resolvedAction = normalizeAction(action);
        String resolvedPrompt = prompt == null ? "" : prompt;
        String resolvedSystemPrompt = hasText(systemPrompt) ? systemPrompt : buildSystemPrompt(resolvedAction);

        try {
            log.debug("Generating text for action: {}, prompt length: {}", resolvedAction, resolvedPrompt.length());

            ChatResponse response = aiChatService.chat(ChatRequest.builder()
                    .message(resolvedPrompt)
                    .systemPrompt(resolvedSystemPrompt)
                    .build());

            if (response == null || response.getContent() == null) {
                throw new IllegalStateException("AI provider returned an empty response");
            }

            log.info("Text generation completed for action: {}", resolvedAction);
            return response.getContent();
        } catch (Exception e) {
            log.error("Text generation failed for action: {}", resolvedAction, e);
            throw new IllegalStateException("Text generation failed: " + e.getMessage(), e);
        }
    }

    public String generateArticle(String title, String keywords, String description) {
        String prompt = String.format(
                "Write an article about \"%s\".%nKeywords: %s%nRequirements: %s",
                defaultString(title),
                defaultString(keywords),
                hasText(description) ? description : "Use concrete details and a clear structure.");
        return generate("article", prompt);
    }

    public String generateAdCopy(String productName, String features, String targetAudience) {
        String prompt = String.format(
                "Write ad copy for \"%s\".%nFeatures: %s%nTarget audience: %s",
                defaultString(productName),
                defaultString(features),
                defaultString(targetAudience));
        return generate("ad_copy", prompt);
    }

    public String generatePoem(String theme, String style) {
        String prompt = String.format(
                "Write a poem about \"%s\".%nStyle: %s",
                defaultString(theme),
                hasText(style) ? style : "modern");
        return generate("poem", prompt);
    }

    private String buildSystemPrompt(String action) {
        return switch (normalizeAction(action)) {
            case "article" -> "You are a professional content writer. Create structured, accurate, readable articles with clear sections.";
            case "novel" -> "You are a fiction writer. Build vivid scenes, coherent characters, conflict, and narrative momentum.";
            case "ad_copy" -> "You are a conversion-focused copywriter. Write concise, persuasive copy with a clear value proposition.";
            case "poem" -> "You are a poet. Write expressive poems with coherent imagery, rhythm, and tone.";
            case "email" -> "You are a business communication expert. Write clear, polite, well-structured email content.";
            default -> "You are a helpful AI assistant. Produce accurate, useful, and well-organized text.";
        };
    }

    private String normalizeAction(String action) {
        return hasText(action) ? action.trim().toLowerCase() : "generate";
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private static String defaultString(String value) {
        return value == null ? "" : value;
    }
}
