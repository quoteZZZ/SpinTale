package com.spintale.ai.core;

import lombok.Builder;
import lombok.Data;

/**
 * 工具调用
 */
@Data
@Builder
public class ToolCall {
    
    /**
     * 工具调用 ID
     */
    private String id;
    
    /**
     * 工具名称
     */
    private String name;
    
    /**
     * 工具参数 (JSON 字符串)
     */
    private String arguments;
}
