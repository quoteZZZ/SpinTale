package com.spintale.ai.core.exception;

/**
 * Base exception for AI service errors.
 */
public class AiServiceException extends RuntimeException {

    private final String errorCode;
    private final transient Object context;

    public AiServiceException(String message) {
        super(message);
        this.errorCode = "AI_SERVICE_ERROR";
        this.context = null;
    }

    public AiServiceException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "AI_SERVICE_ERROR";
        this.context = null;
    }

    public AiServiceException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.context = null;
    }

    public AiServiceException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.context = null;
    }

    public AiServiceException(String errorCode, String message, Object context) {
        super(message);
        this.errorCode = errorCode;
        this.context = context;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public Object getContext() {
        return context;
    }
}
