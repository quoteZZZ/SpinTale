package com.spintale.ai.generation.template;

import org.springframework.stereotype.Component;
import java.util.Map;

/**
 * 广告词/文案生成模板
 */
@Component
public class AdCopyTemplate implements ContentTemplate {

    @Override
    public String getContentType() {
        return "ad_copy";
    }

    @Override
    public String getSystemPrompt() {
        return "你是一位资深广告文案策划师，擅长创作简洁有力、富有感染力的广告语和营销文案。" +
               "你能够精准把握产品卖点，用精炼的语言打动目标受众。";
    }

    @Override
    public String buildPrompt(Map<String, Object> params) {
        StringBuilder prompt = new StringBuilder();
        
        String title = (String) params.getOrDefault("title", "");
        String keywords = (String) params.getOrDefault("keywords", "");
        String description = (String) params.getOrDefault("description", "");
        String targetAudience = (String) params.getOrDefault("targetAudience", "大众消费者");
        String tone = (String) params.getOrDefault("tone", "活力");
        String language = (String) params.getOrDefault("language", "zh");
        
        // 广告特有参数
        String productName = (String) params.getOrDefault("productName", title);
        String productFeatures = (String) params.getOrDefault("productFeatures", "");
        String callToAction = (String) params.getOrDefault("callToAction", "");
        String platform = (String) params.getOrDefault("platform", "通用");

        prompt.append("请为以下产品/服务创作广告文案：\n\n");
        
        if (productName != null && !productName.isEmpty()) {
            prompt.append("【产品/品牌名称】").append(productName).append("\n");
        }
        
        if (description != null && !description.isEmpty()) {
            prompt.append("【产品描述】\n").append(description).append("\n\n");
        }
        
        if (productFeatures != null && !productFeatures.isEmpty()) {
            prompt.append("【核心卖点】\n").append(productFeatures).append("\n\n");
        }
        
        if (keywords != null && !keywords.isEmpty()) {
            prompt.append("【关键词】\n").append(keywords).append("\n\n");
        }
        
        prompt.append("【目标受众】").append(targetAudience).append("\n");
        prompt.append("【文案风格】").append(tone).append("\n");
        
        if (platform != null && !platform.isEmpty()) {
            prompt.append("【投放平台】").append(platform).append("\n");
        }
        
        if ("zh".equals(language)) {
            prompt.append("【语言】中文\n\n");
        } else if ("en".equals(language)) {
            prompt.append("【Language】English\n\n");
        }

        prompt.append("创作要求：\n");
        prompt.append("1. 创作 5-10 条不同风格的广告语（短句，朗朗上口）\n");
        prompt.append("2. 撰写 1-2 段完整的广告文案（100-200 字）\n");
        prompt.append("3. 突出产品独特卖点和用户价值\n");
        prompt.append("4. 语言简洁有力，易于记忆和传播\n");
        prompt.append("5. 适当运用修辞手法（对偶、排比、双关等）\n");
        
        if (callToAction != null && !callToAction.isEmpty()) {
            prompt.append("6. 包含行动号召：").append(callToAction).append("\n");
        } else {
            prompt.append("6. 包含明确的行动号召（CTA）\n");
        }
        
        prompt.append("\n输出格式：\n");
        prompt.append("### 广告语集合\n");
        prompt.append("1. ...\n");
        prompt.append("2. ...\n\n");
        prompt.append("### 完整文案\n");
        prompt.append("[文案内容]\n");

        return prompt.toString();
    }

    @Override
    public String postProcess(String content, Map<String, Object> params) {
        // 可以添加后处理逻辑，如提取关键广告语、格式化等
        return content;
    }
}
