package com.spintale.ai.generation.template;

import org.springframework.stereotype.Component;
import java.util.Map;

/**
 * 小说/故事创作模板
 */
@Component
public class NovelTemplate implements ContentTemplate {

    @Override
    public String getContentType() {
        return "novel";
    }

    @Override
    public String getSystemPrompt() {
        return "你是一位富有创意的小说作家，擅长创作引人入胜的故事情节和立体的人物形象。" +
               "你能够根据用户提供的主题、设定和要求，创作出精彩的小说或故事。";
    }

    @Override
    public String buildPrompt(Map<String, Object> params) {
        StringBuilder prompt = new StringBuilder();
        
        String title = (String) params.getOrDefault("title", "未指定标题");
        String keywords = (String) params.getOrDefault("keywords", "");
        String description = (String) params.getOrDefault("description", "");
        String tone = (String) params.getOrDefault("tone", "生动");
        String length = (String) params.getOrDefault("length", "medium");
        String language = (String) params.getOrDefault("language", "zh");
        
        // 小说特有参数
        String genre = (String) params.getOrDefault("genre", "现代");
        String characters = (String) params.getOrDefault("characters", "");
        String setting = (String) params.getOrDefault("setting", "");
        String plotPoints = (String) params.getOrDefault("plotPoints", "");

        prompt.append("请帮我创作一部名为「").append(title).append("」的小说/故事。\n\n");
        
        if (genre != null && !genre.isEmpty()) {
            prompt.append("【题材类型】").append(genre).append("\n");
        }
        
        if (description != null && !description.isEmpty()) {
            prompt.append("【故事概要】\n").append(description).append("\n\n");
        }
        
        if (characters != null && !characters.isEmpty()) {
            prompt.append("【主要人物】\n").append(characters).append("\n\n");
        }
        
        if (setting != null && !setting.isEmpty()) {
            prompt.append("【故事背景】\n").append(setting).append("\n\n");
        }
        
        if (keywords != null && !keywords.isEmpty()) {
            prompt.append("【关键元素】\n").append(keywords).append("\n\n");
        }
        
        if (plotPoints != null && !plotPoints.isEmpty()) {
            prompt.append("【情节要点】\n").append(plotPoints).append("\n\n");
        }
        
        prompt.append("【叙事风格】").append(tone).append("\n");
        prompt.append("【篇幅】");
        
        switch (length) {
            case "short":
                prompt.append("短篇（1000-3000 字）");
                break;
            case "long":
                prompt.append("长篇（10000 字以上，可分章节）");
                break;
            default:
                prompt.append("中篇（3000-8000 字）");
        }
        prompt.append("\n");
        
        if ("zh".equals(language)) {
            prompt.append("【语言】中文\n\n");
        } else if ("en".equals(language)) {
            prompt.append("【Language】English\n\n");
        }

        prompt.append("创作要求：\n");
        prompt.append("1. 开篇要吸引人，快速建立故事氛围\n");
        prompt.append("2. 人物形象要鲜明，性格特点突出\n");
        prompt.append("3. 情节发展要有起伏，设置适当的冲突和转折\n");
        prompt.append("4. 对话要自然，符合人物身份和性格\n");
        prompt.append("5. 环境描写要生动，增强代入感\n");
        prompt.append("6. 结尾要有余韵，给人思考空间\n\n");
        
        prompt.append("如果是长篇故事，请合理分章节，每章有小标题。\n");

        return prompt.toString();
    }
}
