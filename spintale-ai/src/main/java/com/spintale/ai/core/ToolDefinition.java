package com.spintale.ai.core;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * 工具定义
 */
@Data
@Builder
public class ToolDefinition {
    
    /**
     * 工具名称
     */
    private String name;
    
    /**
     * 工具描述
     */
    private String description;
    
    /**
     * 参数 schema (JSON Schema)
     */
    private Map<String, Object> parameters;
}
