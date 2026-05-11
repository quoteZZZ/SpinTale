package com.spintale.ai.generation.template;

import org.springframework.stereotype.Component;
import java.util.Map;

/**
 * 文章生成模板
 */
@Component
public class ArticleTemplate implements ContentTemplate {

    @Override
    public String getContentType() {
        return "article";
    }

    @Override
    public String getSystemPrompt() {
        return "你是一位专业的文章写手，擅长撰写结构清晰、内容充实、语言流畅的文章。" +
               "你能够根据主题、关键词和用户需求，创作出高质量的文章内容。";
    }

    @Override
    public String buildPrompt(Map<String, Object> params) {
        StringBuilder prompt = new StringBuilder();
        
        String title = (String) params.getOrDefault("title", "未指定标题");
        String keywords = (String) params.getOrDefault("keywords", "");
        String description = (String) params.getOrDefault("description", "");
        String targetAudience = (String) params.getOrDefault("targetAudience", "普通读者");
        String tone = (String) params.getOrDefault("tone", "专业");
        String length = (String) params.getOrDefault("length", "medium");
        String language = (String) params.getOrDefault("language", "zh");

        prompt.append("请帮我写一篇关于「").append(title).append("」的文章。\n\n");
        
        if (description != null && !description.isEmpty()) {
            prompt.append("【文章要求】\n").append(description).append("\n\n");
        }
        
        if (keywords != null && !keywords.isEmpty()) {
            prompt.append("【关键词】\n").append(keywords).append("\n\n");
        }
        
        prompt.append("【目标读者】").append(targetAudience).append("\n");
        prompt.append("【语气风格】").append(tone).append("\n");
        prompt.append("【文章长度】");
        
        switch (length) {
            case "short":
                prompt.append("简短（300-500 字）");
                break;
            case "long":
                prompt.append("详细（1500 字以上）");
                break;
            default:
                prompt.append("适中（800-1200 字）");
        }
        prompt.append("\n");
        
        if ("zh".equals(language)) {
            prompt.append("【语言】中文\n\n");
        } else if ("en".equals(language)) {
            prompt.append("【Language】English\n\n");
        }

        prompt.append("请按照以下结构撰写文章：\n");
        prompt.append("1. 引人入胜的开头，点明主题\n");
        prompt.append("2. 主体部分，分段落详细阐述，逻辑清晰\n");
        prompt.append("3. 有力的结尾，总结全文或提出展望\n\n");
        
        prompt.append("注意：\n");
        prompt.append("- 内容要原创，不要抄袭\n");
        prompt.append("- 语言要流畅自然\n");
        prompt.append("- 适当使用例子和数据支撑观点\n");
        prompt.append("- 段落之间过渡要自然\n");

        return prompt.toString();
    }
}
