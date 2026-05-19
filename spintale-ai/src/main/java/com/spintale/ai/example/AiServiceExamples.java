package com.spintale.ai.example;

import com.spintale.ai.core.annotation.*;

/**
 * 声明式AI服务示例
 * 
 * 展示如何使用注解快速创建AI服务，无需编写实现代码
 * 
 * @author SpinTale AI Team
 */
public class AiServiceExamples {
    
    // ==================== 示例1: 基础客服助手 ====================
    
    /**
     * 客服助手服务
     * 
     * 使用方式:
     * <pre>{@code
     * @Autowired
     * private CustomerSupport customerSupport;
     * 
     * String answer = customerSupport.answerQuestion("如何退货？");
     * }</pre>
     */
    @AiService(name = "customer-support")
    @SystemMessage("你是专业的电商客服助手，友好、耐心地回答用户问题。" +
                   "如果不确定，请诚实地告知用户。")
    public interface CustomerSupport {
        
        /**
         * 回答用户问题
         */
        String answerQuestion(@UserMessage String question);
        
        /**
         * 处理投诉（使用更低的温度以获得更稳定的回复）
         */
        @Temperature(0.3)
        String handleComplaint(@UserMessage String complaint);
    }
    
    // ==================== 示例2: 多语言翻译服务 ====================
    
    /**
     * 翻译服务
     */
    @AiService(name = "translator", model = "gpt-4")
    @SystemMessage("你是专业的翻译专家，精通多国语言。" +
                   "翻译时要保持原文的语气和风格。")
    public interface Translator {
        
        /**
         * 翻译成中文
         */
        @SystemMessage("请将以下文本翻译成简体中文")
        String translateToChinese(@UserMessage String text);
        
        /**
         * 翻译成英文
         */
        @SystemMessage("请将以下文本翻译成英语")
        String translateToEnglish(@UserMessage String text);
        
        /**
         * 通用翻译（指定目标语言）
         */
        String translate(@UserMessage String text, @Language String targetLanguage);
    }
    
    // ==================== 示例3: 内容生成服务 ====================
    
    /**
     * 内容生成服务
     */
    @AiService(name = "content-generator")
    public interface ContentGenerator {
        
        /**
         * 生成博客文章大纲
         */
        @SystemMessage("你是资深内容创作者，擅长撰写技术博客。" +
                       "请为给定主题生成详细的博客大纲，包含5-7个章节。")
        @Temperature(0.8)  // 高创造性
        String generateBlogOutline(@UserMessage String topic);
        
        /**
         * 生成营销文案
         */
        @SystemMessage("你是营销文案专家，擅长创作吸引人的广告语。")
        @Temperature(0.9)
        String generateMarketingCopy(@UserMessage String productDescription);
        
        /**
         * 代码审查建议
         */
        @SystemMessage("你是资深软件工程师，擅长代码审查。" +
                       "请分析代码并提供改进建议。")
        @Temperature(0.2)  // 低温度，更精确
        String reviewCode(@UserMessage String code);
    }
    
    // ==================== 示例4: 数据分析服务 ====================
    
    /**
     * 数据分析助手
     */
    @AiService(name = "data-analyst")
    @SystemMessage("你是数据分析师，擅长从数据中发现洞察。")
    public interface DataAnalyst {
        
        /**
         * 解释数据趋势
         */
        String explainTrend(@UserMessage String dataDescription);
        
        /**
         * 生成SQL查询
         */
        @SystemMessage("你是SQL专家，请根据需求生成优化的SQL查询语句。")
        String generateSQL(@UserMessage String requirement);
        
        /**
         * 数据可视化建议
         */
        @SystemMessage("你是数据可视化专家，请推荐最适合的图表类型。")
        String suggestVisualization(@UserMessage String dataType);
    }
}
