package com.spintale.ai.generation.service;

import dev.langchain4j.model.chat.ChatModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 文本生成服务
 * 基于 LangChain4j ChatModel 实现通用文本生成能力
 */
@Service
public class TextGenerationService {

    private static final Logger log = LoggerFactory.getLogger(TextGenerationService.class);

    private final ChatModel chatModel;

    public TextGenerationService(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    /**
     * 生成文本内容
     * @param action 生成动作（article, novel, ad_copy 等）
     * @param prompt 提示词
     * @return 生成的文本
     */
    public String generate(String action, String prompt) {
        return generate(action, prompt, null);
    }

    /**
     * 生成文本内容（带系统提示）
     * @param action 生成动作
     * @param prompt 用户提示词
     * @param systemPrompt 系统提示词
     * @return 生成的文本
     */
    public String generate(String action, String prompt, String systemPrompt) {
        try {
            StringBuilder fullPrompt = new StringBuilder();

            // 根据动作类型添加特定指令
            if (systemPrompt != null && !systemPrompt.isEmpty()) {
                fullPrompt.append(systemPrompt).append("\n\n");
            } else {
                fullPrompt.append(buildSystemPrompt(action)).append("\n\n");
            }

            fullPrompt.append(prompt);

            log.debug("Generating text for action: {}, prompt length: {}", 
                    action, prompt != null ? prompt.length() : 0);

            String response = chatModel.chat(fullPrompt.toString());

            log.info("Text generation completed for action: {}", action);
            return response;

        } catch (Exception e) {
            log.error("Text generation failed for action: {}", action, e);
            throw new RuntimeException("文本生成失败：" + e.getMessage(), e);
        }
    }

    /**
     * 构建系统提示词
     */
    private String buildSystemPrompt(String action) {
        switch (action.toLowerCase()) {
            case "article":
                return "你是一位专业的内容创作者，擅长撰写高质量的文章。" +
                       "请根据用户提供的主题和要求，创作结构清晰、内容丰富、语言流畅的文章。" +
                       "文章应包含引言、正文和结论，适当使用小标题分段。";

            case "novel":
                return "你是一位创意小说家，擅长创作引人入胜的故事。" +
                       "请根据用户提供的设定，创作情节生动、人物鲜明、对话自然的小说章节。" +
                       "注意保持故事连贯性，营造适当的氛围和情感张力。";

            case "ad_copy":
                return "你是一位资深广告文案策划，擅长创作有吸引力的营销文案。" +
                       "请根据产品信息和目标受众，创作简洁有力、突出卖点、激发购买欲的广告文案。" +
                       "文案应具有感染力，能够引起目标受众的共鸣。";

            case "poem":
                return "你是一位才华横溢的诗人，擅长创作各种风格的诗歌。" +
                       "请根据用户提供的主题，创作意境优美、韵律和谐、富有感染力的诗歌。";

            case "email":
                return "你是一位专业的商务沟通专家，擅长撰写得体高效的商务邮件。" +
                       "请根据用户需求，创作格式规范、语气恰当、表达清晰的邮件内容。";

            default:
                return "你是一位智能助手，擅长根据用户需求生成高质量的文本内容。" +
                       "请理解用户意图，提供准确、有用、条理清晰的回复。";
        }
    }

    /**
     * 生成文章
     */
    public String generateArticle(String title, String keywords, String description) {
        String prompt = String.format(
                "请写一篇关于\"%s\"的文章。\n" +
                "关键词：%s\n" +
                "要求：%s",
                title, keywords, description != null ? description : "内容详实，结构完整");
        return generate("article", prompt);
    }

    /**
     * 生成广告文案
     */
    public String generateAdCopy(String productName, String features, String targetAudience) {
        String prompt = String.format(
                "请为产品\"%s\"创作广告文案。\n" +
                "产品特点：%s\n" +
                "目标受众：%s",
                productName, features, targetAudience);
        return generate("ad_copy", prompt);
    }

    /**
     * 生成诗歌
     */
    public String generatePoem(String theme, String style) {
        String prompt = String.format(
                "请以\"%s\"为主题创作一首诗。\n" +
                "风格：%s",
                theme, style != null ? style : "现代诗");
        return generate("poem", prompt);
    }
}
