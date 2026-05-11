package com.spintale.ai.prompt;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 基于字符串替换的提示词模板实现
 * 
 * 支持语法：{{variable_name}}
 */
@Slf4j
public class SimplePromptTemplate implements PromptTemplate {
    
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{(\\w+)\\}\\}");
    
    @Override
    public String render(String template, Map<String, Object> variables) {
        if (template == null || variables == null) {
            return template;
        }
        
        StringBuilder result = new StringBuilder(template);
        Matcher matcher = VARIABLE_PATTERN.matcher(template);
        
        // 由于 Matcher 会改变位置，我们需要重新构建
        StringBuffer sb = new StringBuffer();
        matcher = VARIABLE_PATTERN.matcher(template);
        while (matcher.find()) {
            String variableName = matcher.group(1);
            Object value = variables.get(variableName);
            String replacement = value != null ? value.toString() : "";
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);
        
        log.debug("Rendered template with {} variables", variables.size());
        return sb.toString();
    }
    
    @Override
    public String loadTemplate(String resourcePath) {
        try {
            ClassPathResource resource = new ClassPathResource(resourcePath);
            byte[] bytes = FileCopyUtils.copyToByteArray(resource.getInputStream());
            String content = new String(bytes, StandardCharsets.UTF_8);
            log.debug("Loaded template from: {}", resourcePath);
            return content;
        } catch (IOException e) {
            log.error("Failed to load template from {}: {}", resourcePath, e.getMessage());
            throw new RuntimeException("Failed to load template", e);
        }
    }
    
    @Override
    public String renderFile(String resourcePath, Map<String, Object> variables) {
        String template = loadTemplate(resourcePath);
        return render(template, variables);
    }
}
