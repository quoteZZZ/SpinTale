package com.spintale.ai.skill;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

/**
 * Lightweight built-in skill for explaining code snippets.
 */
@Service
public class CodeExplanationSkill implements AiSkill {

    @Override
    public String getId() {
        return "code_explanation";
    }

    @Override
    public String getName() {
        return "Code explanation";
    }

    @Override
    public String getDescription() {
        return "Explains code snippets, algorithms, design patterns, and implementation details.";
    }

    @Override
    public String[] getTags() {
        return new String[] {"coding", "education", "analysis"};
    }

    @Override
    public Map<String, Object> getParametersSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        properties.put("code", property("string", "Code snippet to explain"));
        properties.put("language", property("string", "Programming language. Default: auto-detect"));
        properties.put("explanationLevel", property("string", "basic, intermediate, or advanced"));
        properties.put("includeExamples", property("boolean", "Whether to include usage examples"));

        schema.put("properties", properties);
        schema.put("required", new String[] {"code"});
        return schema;
    }

    @Override
    public SkillResult execute(Map<String, Object> args) {
        String code = value(args, "code", "");
        String language = value(args, "language", "auto-detect");
        String level = value(args, "explanationLevel", "intermediate");
        boolean includeExamples = Boolean.parseBoolean(String.valueOf(args.getOrDefault("includeExamples", true)));

        if (code.isBlank()) {
            return SkillResult.error("code is required");
        }

        StringBuilder explanation = new StringBuilder();
        explanation.append("Code explanation\n\n");
        explanation.append("Language: ").append(language).append("\n");
        explanation.append("Level: ").append(level).append("\n\n");
        explanation.append("Summary\n");
        explanation.append("This snippet should be reviewed by the configured AI model for a full semantic explanation.\n\n");
        explanation.append("Structure\n");
        explanation.append("- Lines: ").append(code.split("\\R").length).append("\n");
        explanation.append("- Characters: ").append(code.length()).append("\n");
        if (includeExamples) {
            explanation.append("\nExample prompt\n");
            explanation.append("Explain the following ").append(language).append(" code at ").append(level).append(" level.\n");
        }

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("language", language);
        metadata.put("level", level);
        metadata.put("linesOfCode", code.split("\\R").length);

        return SkillResult.success(explanation.toString(), metadata);
    }

    private Map<String, Object> property(String type, String description) {
        Map<String, Object> property = new HashMap<>();
        property.put("type", type);
        property.put("description", description);
        return property;
    }

    private String value(Map<String, Object> args, String key, String defaultValue) {
        if (args == null) {
            return defaultValue;
        }
        Object value = args.get(key);
        return value == null ? defaultValue : String.valueOf(value).trim();
    }
}
