package com.spintale.ai.config;

/**
 * AI 服务异常基类
 */
public class AiServiceException extends RuntimeException {
    
    private final String errorCode;
    private final int statusCode;
    
    public AiServiceException(String message) {
        super(message);
        this.errorCode = "AI_SERVICE_ERROR";
        this.statusCode = 500;
    }
    
    public AiServiceException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "AI_SERVICE_ERROR";
        this.statusCode = 500;
    }
    
    public AiServiceException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.statusCode = 500;
    }
    
    public AiServiceException(String errorCode, String message, int statusCode) {
        super(message);
        this.errorCode = errorCode;
        this.statusCode = statusCode;
    }
    
    public AiServiceException(String errorCode, String message, int statusCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.statusCode = statusCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public int getStatusCode() {
        return statusCode;
    }
}
