package com.spintale.ai.agent.react.api;

import java.util.Map;
import java.util.List;

/**
 * AI Agent 执行结果
 */
public interface AgentResult {
    
    /**
     * 是否成功
     */
    boolean isSuccess();
    
    /**
     * 最终回复内容
     */
    String getContent();
    
    /**
     * 执行过程中的中间步骤
     */
    Map<String, Object> getSteps();
    
    /**
     * 使用的工具列表
     */
    List<String> getUsedTools();
    
    /**
     * Token 使用情况
     */
    Object getTokenUsage();
    
    /**
     * Tool execution result for Temporal workflow.
     */
    class ToolExecutionResult {
        private String toolName;
        private String result;
        private boolean success;
        private String errorMessage;
        private long executionTimeMs;
        
        public ToolExecutionResult() {
        }
        
        public ToolExecutionResult(String toolName, String result, boolean success) {
            this.toolName = toolName;
            this.result = result;
            this.success = success;
        }
        
        public String getToolName() {
            return toolName;
        }
        
        public void setToolName(String toolName) {
            this.toolName = toolName;
        }
        
        public String getResult() {
            return result;
        }
        
        public void setResult(String result) {
            this.result = result;
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public void setSuccess(boolean success) {
            this.success = success;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
        
        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }
        
        public long getExecutionTimeMs() {
            return executionTimeMs;
        }
        
        public void setExecutionTimeMs(long executionTimeMs) {
            this.executionTimeMs = executionTimeMs;
        }
        
        @Override
        public String toString() {
            return "ToolExecutionResult{" +
                "toolName='" + toolName + '\'' +
                ", success=" + success +
                ", result='" + (result != null ? result.substring(0, Math.min(50, result.length())) : "null") + "...'" +
                '}';
        }
    }
}
