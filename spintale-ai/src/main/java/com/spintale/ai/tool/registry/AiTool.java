package com.spintale.ai.tool.registry;

import java.util.Map;

/**
 * 标准化工具接口
 * 
 * 参考 LangChain4j 的 @Tool 和 Spring AI 的 FunctionCallback 设计，
 * 提供统一的工具调用接口。
 * 
 * 使用示例：
 * <pre>{@code
 * @Component
 * public class WeatherTool implements AiTool {
 *     
 *     @Override
 *     public String getName() {
 *         return "get_weather";
 *     }
 *     
 *     @Override
 *     public String getDescription() {
 *         return "获取指定城市的天气信息";
 *     }
 *     
 *     @Override
 *     public ToolSchema getSchema() {
 *         return ToolSchema.builder()
 *             .addProperty("city", "string", "城市名称", true)
 *             .build();
 *     }
 *     
 *     @Override
 *     public String execute(Map<String, Object> arguments) {
 *         String city = (String) arguments.get("city");
 *         return "今天" + city + "的天气是晴天";
 *     }
 * }
 * }</pre>
 */
public interface AiTool {

    /**
     * 工具名称（唯一标识）
     */
    String getName();

    /**
     * 工具描述（用于LLM理解工具用途）
     */
    String getDescription();

    /**
     * 工具参数schema
     */
    ToolSchema getSchema();

    /**
     * 执行工具
     *
     * @param arguments 工具参数
     * @return 执行结果
     */
    String execute(Map<String, Object> arguments);

    /**
     * 是否启用（可用于动态控制工具可用性）
     */
    default boolean isEnabled() {
        return true;
    }

    /**
     * 工具 Schema 定义
     */
    class ToolSchema {
        private final Map<String, PropertyDefinition> properties;
        private final java.util.List<String> required;

        private ToolSchema(Builder builder) {
            this.properties = builder.properties;
            this.required = builder.required;
        }

        public Map<String, PropertyDefinition> getProperties() {
            return properties;
        }

        public java.util.List<String> getRequired() {
            return required;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private final java.util.Map<String, PropertyDefinition> properties = new java.util.LinkedHashMap<>();
            private final java.util.List<String> required = new java.util.ArrayList<>();

            public Builder addProperty(String name, String type, String description, boolean required) {
                properties.put(name, new PropertyDefinition(type, description));
                if (required) {
                    this.required.add(name);
                }
                return this;
            }

            public ToolSchema build() {
                return new ToolSchema(this);
            }
        }

        public record PropertyDefinition(String type, String description) {}
    }
}
