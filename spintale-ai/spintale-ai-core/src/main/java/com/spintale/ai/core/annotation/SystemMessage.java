package com.spintale.ai.core.annotation;

import java.lang.annotation.*;

/**
 * 系统消息注解
 * 
 * 用于定义AI助手的角色和行为
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SystemMessage {
    
    /**
     * 系统提示词内容
     * 支持变量占位符，如: {name}, {role}
     */
    String value();
}
