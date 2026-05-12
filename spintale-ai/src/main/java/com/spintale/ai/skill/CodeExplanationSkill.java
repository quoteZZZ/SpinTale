package com.spintale.ai.skill;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 代码解释技能
 * 
 * 用于解释代码片段、算法、设计模式等
 */
@Service
public class CodeExplanationSkill implements AiSkill {
    
    @Override
    public String getId() {
        return "code_explanation";
    }
    
    @Override
    public String getName() {
        return "代码解释";
    }
    
    @Override
    public String getDescription() {
        return "解释代码片段、算法原理、设计模式或技术概念。支持多种编程语言。";
    }
    
    @Override
    public String[] getTags() {
        return new String[]{"coding", "education", "analysis"};
    }
    
    @Override
    public Map<String, Object> getParametersSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");
        
        Map<String, Object> properties = new HashMap<>();
        
        // code: 待解释的代码
        Map<String, Object> codeProp = new HashMap<>();
        codeProp.put("type", "string");
        codeProp.put("description", "需要解释的代码片段");
        properties.put("code", codeProp);
        
        // language: 编程语言
        Map<String, Object> languageProp = new HashMap<>();
        languageProp.put("type", "string");
        languageProp.put("description", "编程语言，如 Java, Python, JavaScript 等");
        languageProp.put("default", "auto-detect");
        properties.put("language", languageProp);
        
        // explanationLevel: 解释深度
        Map<String, Object> levelProp = new HashMap<>();
        levelProp.put("type", "string");
        levelProp.put("description", "解释深度：basic(基础), intermediate(中等), advanced(高级)");
        levelProp.put("enum", new String[]{"basic", "intermediate", "advanced"});
        levelProp.put("default", "intermediate");
        properties.put("explanationLevel", levelProp);
        
        // includeExamples: 是否包含示例
        Map<String, Object> examplesProp = new HashMap<>();
        examplesProp.put("type", "boolean");
        examplesProp.put("description", "是否包含使用示例");
        examplesProp.put("default", true);
        properties.put("includeExamples", examplesProp);
        
        schema.put("properties", properties);
        schema.put("required", new String[]{"code"});
        
        return schema;
    }
    
    @Override
    public SkillResult execute(Map<String, Object> args) {
        String code = (String) args.get("code");
        String language = (String) args.getOrDefault("language", "auto-detect");
        String level = (String) args.getOrDefault("explanationLevel", "intermediate");
        Boolean includeExamples = (Boolean) args.getOrDefault("includeExamples", true);
        
        if (code == null || code.trim().isEmpty()) {
            return SkillResult.error("代码不能为空");
        }
        
        // 构建解释请求
        StringBuilder explanation = new StringBuilder();
        explanation.append("## 代码解释\n\n");
        explanation.append("**语言**: ").append(language).append("\n");
        explanation.append("**难度**: ").append(level).append("\n\n");
        
        explanation.append("### 功能概述\n");
        explanation.append("这段代码的主要功能是...\n\n");
        
        explanation.append("### 核心逻辑\n");
        explanation.append("1. 首先...\n");
        explanation.append("2. 然后...\n");
        explanation.append("3. 最后...\n\n");
        
        if (includeExamples) {
            explanation.append("### 使用示例\n");
            explanation.append("```").append(language).append("\n");
            explanation.append("// 示例代码\n");
            explanation.append("```\n\n");
        }
        
        explanation.append("### 注意事项\n");
        explanation.append("- 注意边界条件处理\n");
        explanation.append("- 性能优化建议\n");
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("language", language);
        metadata.put("complexity", "medium");
        metadata.put("linesOfCode", code.split("\n").length);
        
        return SkillResult.success(explanation.toString(), metadata);
    }
}
