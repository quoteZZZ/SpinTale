package com.spintale.ai.web.advice;

import com.spintale.ai.core.exception.AiServiceException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * AI 模块全局异常处理器
 */
@RestControllerAdvice(basePackages = "com.spintale.ai")
public class AiGlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(AiGlobalExceptionHandler.class);

    @ExceptionHandler(AiServiceException.class)
    public ResponseEntity<Map<String, Object>> handleAiServiceException(AiServiceException ex) {
        log.error("AI service exception: {}", ex.getMessage(), ex);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("errorCode", ex.getErrorCode());
        response.put("message", ex.getMessage());
        response.put("statusCode", ex.getStatusCode());
        response.put("timestamp", LocalDateTime.now().toString());
        
        return ResponseEntity.status(ex.getStatusCode()).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("Invalid argument: {}", ex.getMessage());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("errorCode", "INVALID_ARGUMENT");
        response.put("message", ex.getMessage());
        response.put("statusCode", HttpStatus.BAD_REQUEST.value());
        response.put("timestamp", LocalDateTime.now().toString());
        
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        log.error("Unexpected error in AI module", ex);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("errorCode", "INTERNAL_ERROR");
        response.put("message", "An unexpected error occurred. Please try again later.");
        response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.put("timestamp", LocalDateTime.now().toString());
        
        return ResponseEntity.internalServerError().body(response);
    }
}
