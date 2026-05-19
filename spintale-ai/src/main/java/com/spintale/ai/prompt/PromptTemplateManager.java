package com.spintale.ai.prompt;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Prompt 模板管理器
 * 
 * 参考 LangChain 的 PromptTemplate 设计，提供：
 * - 模板注册和管理
 * - 变量替换
 * - 版本控制
 * - 分类管理
 * 
 * 使用示例：
 * <pre>{@code
 * // 注册模板
 * templateManager.register("greeting", "你好，{name}！我是{assistant}。");
 * 
 * // 渲染模板
 * String prompt = templateManager.render("greeting", 
 *     Map.of("name", "张三", "assistant", "AI助手"));
 * }</pre>
 */
@Slf4j
public class PromptTemplateManager {

    private final Map<String, PromptTemplate> templates = new ConcurrentHashMap<>();
    private final Map<String, Map<String, PromptTemplate>> categorizedTemplates = new ConcurrentHashMap<>();

    /**
     * 注册模板
     *
     * @param name 模板名称
     * @param template 模板内容
     */
    public void register(String name, String template) {
        register(name, template, null);
    }

    /**
     * 注册带分类的模板
     *
     * @param name 模板名称
     * @param template 模板内容
     * @param category 分类（如：chat、rag、agent等）
     */
    public void register(String name, String template, String category) {
        PromptTemplate promptTemplate = new PromptTemplate();
        promptTemplate.setName(name);
        promptTemplate.setTemplate(template);
        promptTemplate.setCategory(category);
        promptTemplate.setVersion(1);
        
        templates.put(name, promptTemplate);
        
        if (category != null) {
            categorizedTemplates
                .computeIfAbsent(category, k -> new HashMap<>())
                .put(name, promptTemplate);
        }
        
        log.info("Registered prompt template: name={}, category={}", name, category);
    }

    /**
     * 渲染模板
     *
     * @param name 模板名称
     * @param variables 变量映射
     * @return 渲染后的文本
     */
    public String render(String name, Map<String, Object> variables) {
        PromptTemplate template = templates.get(name);
        if (template == null) {
            throw new IllegalArgumentException("Template not found: " + name);
        }
        
        return renderTemplate(template.getTemplate(), variables);
    }

    /**
     * 渲染模板（简单变量）
     *
     * @param name 模板名称
     * @param variables 可变参数
     * @return 渲染后的文本
     */
    public String render(String name, Object... variables) {
        PromptTemplate template = templates.get(name);
        if (template == null) {
            throw new IllegalArgumentException("Template not found: " + name);
        }
        
        Map<String, Object> varMap = new HashMap<>();
        String[] keys = extractVariableKeys(template.getTemplate());
        
        for (int i = 0; i < keys.length && i < variables.length; i++) {
            varMap.put(keys[i], variables[i]);
        }
        
        return renderTemplate(template.getTemplate(), varMap);
    }

    /**
     * 获取模板
     */
    public PromptTemplate getTemplate(String name) {
        return templates.get(name);
    }

    /**
     * 根据分类获取模板列表
     */
    public Map<String, PromptTemplate> getByCategory(String category) {
        return categorizedTemplates.getOrDefault(category, Map.of());
    }

    /**
     * 删除模板
     */
    public void remove(String name) {
        PromptTemplate removed = templates.remove(name);
        if (removed != null && removed.getCategory() != null) {
            Map<String, PromptTemplate> categoryMap = categorizedTemplates.get(removed.getCategory());
            if (categoryMap != null) {
                categoryMap.remove(name);
            }
        }
        log.info("Removed prompt template: {}", name);
    }

    /**
     * 清空所有模板
     */
    public void clear() {
        templates.clear();
        categorizedTemplates.clear();
    }

    /**
     * 获取所有模板
     */
    public Map<String, PromptTemplate> getAllTemplates() {
        return Map.copyOf(templates);
    }

    /**
     * 渲染模板字符串
     */
    private String renderTemplate(String template, Map<String, Object> variables) {
        String result = template;
        
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            String placeholder = "{" + entry.getKey() + "}";
            String value = entry.getValue() != null ? entry.getValue().toString() : "";
            result = result.replace(placeholder, value);
        }
        
        return result;
    }

    /**
     * 提取模板中的变量名
     */
    private String[] extractVariableKeys(String template) {
        // 简单实现：提取 {xxx} 格式的变量
        java.util.List<String> keys = new java.util.ArrayList<>();
        int start = 0;
        
        while ((start = template.indexOf('{', start)) != -1) {
            int end = template.indexOf('}', start);
            if (end != -1) {
                keys.add(template.substring(start + 1, end));
                start = end + 1;
            } else {
                break;
            }
        }
        
        return keys.toArray(new String[0]);
    }

    /**
     * Prompt 模板定义
     */
    @Data
    public static class PromptTemplate {
        private String name;
        private String template;
        private String category;
        private Integer version;
        private String description;
    }
}
