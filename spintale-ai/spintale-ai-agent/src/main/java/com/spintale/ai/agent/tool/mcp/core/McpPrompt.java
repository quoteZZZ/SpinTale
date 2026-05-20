package com.spintale.ai.agent.tool.mcp.core;

import java.util.*;

/**
 * MCP 提示词模板接口
 */
public interface McpPrompt {
    String getId();
    String getName();
    String getDescription();
    List<Argument> getArguments();
    String render(Map<String, Object> args);

    class Argument {
        private String name;
        private String description;
        private boolean required;
        private String defaultValue;

        public Argument(String name, String description, boolean required) {
            this.name = name;
            this.description = description;
            this.required = required;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public boolean isRequired() { return required; }
        public void setRequired(boolean required) { this.required = required; }
        public String getDefaultValue() { return defaultValue; }
        public void setDefaultValue(String defaultValue) { this.defaultValue = defaultValue; }
    }
}
