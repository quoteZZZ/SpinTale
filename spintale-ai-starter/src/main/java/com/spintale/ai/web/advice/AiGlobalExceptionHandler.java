package com.spintale.ai.web.advice;

import com.spintale.ai.core.exception.AiServiceException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice(basePackages = "com.spintale.ai")
public class AiGlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(AiGlobalExceptionHandler.class);

    @ExceptionHandler(AiServiceException.class)
    public ResponseEntity<Map<String, Object>> handleAiServiceException(
            AiServiceException ex,
            HttpServletRequest request) {
        log.error("AI service exception at {}, code={}, message={}",
                request.getRequestURI(), ex.getErrorCode(), ex.getMessage(), ex);
        return error(HttpStatus.BAD_GATEWAY, ex.getErrorCode(), ex.getMessage(), request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(
            IllegalArgumentException ex,
            HttpServletRequest request) {
        log.warn("Invalid AI request at {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        return error(HttpStatus.BAD_REQUEST, "INVALID_ARGUMENT", ex.getMessage(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception ex, HttpServletRequest request) {
        log.error("Unhandled AI exception at {}", request.getRequestURI(), ex);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "AI_INTERNAL_ERROR", "AI service failed", request);
    }

    private ResponseEntity<Map<String, Object>> error(
            HttpStatus status,
            String code,
            String message,
            HttpServletRequest request) {
        return ResponseEntity.status(status).body(Map.of(
                "success", false,
                "code", code,
                "message", message,
                "path", request.getRequestURI(),
                "timestamp", Instant.now().toString()));
    }
}
