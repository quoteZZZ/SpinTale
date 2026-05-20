package com.spintale.ai.web.advice;

import com.spintale.ai.core.exception.AiServiceException;
import com.spintale.common.core.domain.AjaxResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice(basePackages = "com.spintale.ai")
public class AiGlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(AiGlobalExceptionHandler.class);

    @ExceptionHandler(AiServiceException.class)
    public AjaxResult handleAiServiceException(AiServiceException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("AI服务异常，请求地址'{}', 错误码'{}', 错误信息'{}'", 
                  requestURI, e.getErrorCode(), e.getMessage(), e);
        
        return AjaxResult.error(e.getStatusCode(), e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public AjaxResult handleIllegalArgumentException(IllegalArgumentException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("AI参数异常，请求地址'{}', 错误信息'{}'", requestURI, e.getMessage(), e);
        
        return AjaxResult.error("参数错误：" + e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public AjaxResult handleException(Exception e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("AI模块未知异常，请求地址'{}'", requestURI, e);
        
        return AjaxResult.error("系统异常，请稍后重试");
    }
}
