package com.spintale.ai.tool.mcp.core;

import java.util.Map;

/**
 * MCP (Model Context Protocol) 工具接口
 * 
 * MCP 工具是 AI 可以调用的外部功能，如 API 调用、数据库操作、文件处理等
 * 参考：https://modelcontextprotocol.io/
 */
public interface McpTool {
    
    /**
     * 工具唯一标识
     */
    String getId();
    
    /**
     * 工具名称
     */
    String getName();
    
    /**
     * 工具描述（用于 AI 理解何时调用此工具）
     */
    String getDescription();
    
    /**
     * 工具参数 Schema（JSON Schema 格式）
     */
    Map<String, Object> getInputSchema();
    
    /**
     * 执行工具
     * @param args 参数
     * @return 执行结果
     */
    ToolResult execute(Map<String, Object> args);
    
    /**
     * 工具执行结果
     */
    class ToolResult {
        private boolean success;
        private String content;
        private String contentType;  // text, image, resource
        private Object data;
        private String errorMessage;
        
        public ToolResult(boolean success, String content) {
            this.success = success;
            this.content = content;
            this.contentType = "text";
        }
        
        public ToolResult(boolean success, String content, String contentType) {
            this.success = success;
            this.content = content;
            this.contentType = contentType;
        }
        
        public static ToolResult success(String content) {
            return new ToolResult(true, content);
        }
        
        public static ToolResult success(String content, String contentType) {
            return new ToolResult(true, content, contentType);
        }
        
        public static ToolResult error(String message) {
            ToolResult result = new ToolResult(false, null);
            result.errorMessage = message;
            return result;
        }
        
        // Getters and Setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public String getContentType() { return contentType; }
        public void setContentType(String contentType) { this.contentType = contentType; }
        public Object getData() { return data; }
        public void setData(Object data) { this.data = data; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }
}
