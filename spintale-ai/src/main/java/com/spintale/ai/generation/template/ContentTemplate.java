package com.spintale.ai.generation.template;

import java.util.Map;

/**
 * 内容生成模板接口
 * 
 * 定义不同内容类型的 Prompt 模板生成策略
 */
public interface ContentTemplate {

    /**
     * 获取内容类型
     */
    String getContentType();

    /**
     * 构建 Prompt
     * 
     * @param params 参数
     * @return Prompt 文本
     */
    String buildPrompt(Map<String, Object> params);

    /**
     * 获取系统提示词
     * 
     * @return 系统提示词
     */
    default String getSystemPrompt() {
        return "你是一个专业的内容创作助手，擅长根据用户需求生成高质量的内容。";
    }

    /**
     * 后处理生成的内容
     * 
     * @param content 原始内容
     * @param params 参数
     * @return 处理后的内容
     */
    default String postProcess(String content, Map<String, Object> params) {
        return content;
    }
}
