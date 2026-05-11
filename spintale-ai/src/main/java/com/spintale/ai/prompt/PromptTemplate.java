package com.spintale.ai.prompt;

import java.util.Map;

/**
 * 提示词模板引擎接口
 */
public interface PromptTemplate {
    
    /**
     * 渲染模板
     * @param template 模板字符串
     * @param variables 变量映射
     * @return 渲染后的文本
     */
    String render(String template, Map<String, Object> variables);
    
    /**
     * 从文件加载模板
     * @param resourcePath 资源文件路径
     * @return 模板内容
     */
    String loadTemplate(String resourcePath);
    
    /**
     * 渲染文件模板
     * @param resourcePath 资源文件路径
     * @param variables 变量映射
     * @return 渲染后的文本
     */
    String renderFile(String resourcePath, Map<String, Object> variables);
}
