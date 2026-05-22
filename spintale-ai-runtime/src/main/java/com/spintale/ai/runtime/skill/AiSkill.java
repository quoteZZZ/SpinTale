package com.spintale.ai.runtime.skill;

import java.util.Map;

/**
 * Extension point for reusable AI skills.
 */
public interface AiSkill {

    String getId();

    String getName();

    String getDescription();

    default String[] getTags() {
        return new String[] {"general"};
    }

    Map<String, Object> getParametersSchema();

    SkillResult execute(Map<String, Object> args);

    default boolean supportsStreaming() {
        return false;
    }

    default void executeStreaming(Map<String, Object> args, StreamingHandler handler) {
        throw new UnsupportedOperationException("Streaming is not supported for this skill");
    }

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

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public Object getData() {
            return data;
        }

        public void setData(Object data) {
            this.data = data;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        public Map<String, Object> getMetadata() {
            return metadata;
        }

        public void setMetadata(Map<String, Object> metadata) {
            this.metadata = metadata;
        }
    }

    interface StreamingHandler {
        void onToken(String token);

        void onComplete(SkillResult result);

        void onError(String error);
    }
}
