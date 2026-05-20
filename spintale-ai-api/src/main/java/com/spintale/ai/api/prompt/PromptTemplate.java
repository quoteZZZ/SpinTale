package com.spintale.ai.api.prompt;

import java.util.HashMap;
import java.util.Map;

/**
 * Prompt template with variable substitution.
 */
public class PromptTemplate {

    private final String template;
    private final Map<String, Object> variables = new HashMap<>();

    public PromptTemplate(String template) {
        this.template = template;
    }

    /**
     * Set a variable value.
     */
    public PromptTemplate variable(String name, Object value) {
        variables.put(name, value);
        return this;
    }

    /**
     * Render the template with variables substituted.
     */
    public String render() {
        String result = template;
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", 
                                   entry.getValue().toString());
        }
        return result;
    }

    /**
     * Create a new prompt template.
     */
    public static PromptTemplate of(String template) {
        return new PromptTemplate(template);
    }
}
