package com.spintale.ai.skill;

import java.util.Map;

/**
 * AI 技能接口
 * 
 * 参考 Gitee 热门 Java+AI 项目设计：
 * - 支持动态注册/注销
 * - 支持参数 schema 定义
 * - 支持同步/异步执行
 * - 支持流式输出
 */
public interface AiSkill {
    
    /**
     * 技能唯一标识
     */
    String getId();
    
    /**
     * 技能名称（人类可读）
     */
    String getName();
    
    /**
     * 技能描述（用于 AI 理解何时调用此技能）
     */
    String getDescription();
    
    /**
     * 技能分类标签
     */
    default String[] getTags() {
        return new String[]{"general"};
    }
    
    /**
     * 参数 Schema 定义（JSON Schema 格式）
     * 用于 AI 模型理解参数结构
     */
    Map<String, Object> getParametersSchema();
    
    /**
     * 执行技能（同步）
     * @param args 参数 Map
     * @return 执行结果
     */
    SkillResult execute(Map<String, Object> args);
    
    /**
     * 是否支持流式输出
     */
    default boolean supportsStreaming() {
        return false;
    }
    
    /**
     * 执行技能（流式）
     * @param args 参数 Map
     * @param handler 流式处理器
     */
    default void executeStreaming(Map<String, Object> args, StreamingHandler handler) {
        throw new UnsupportedOperationException("Streaming not supported for this skill");
    }
    
    /**
     * 技能执行结果
     */
    class SkillResult {
        private boolean success;
        private Object data;
        private String errorMessage;
        private Map<String, Object> metadata;
        
        public SkillResult(boolean success, Object data) {
            this.success = success;
            this.data = data;
        }
        
        public SkillResult(boolean success, Object data, Map<String, Object> metadata) {
            this(success, data);
            this.metadata = metadata;
        }
        
        public static SkillResult success(Object data) {
            return new SkillResult(true, data);
        }
        
        public static SkillResult success(Object data, Map<String, Object> metadata) {
            return new SkillResult(true, data, metadata);
        }
        
        public static SkillResult error(String message) {
            SkillResult result = new SkillResult(false, null);
            result.errorMessage = message;
            return result;
        }
        
        // Getters and Setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public Object getData() { return data; }
        public void setData(Object data) { this.data = data; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        public Map<String, Object> getMetadata() { return metadata; }
        public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    }
    
    /**
     * 流式处理器接口
     */
    interface StreamingHandler {
        void onToken(String token);
        void onComplete(SkillResult result);
        void onError(String error);
    }
}
