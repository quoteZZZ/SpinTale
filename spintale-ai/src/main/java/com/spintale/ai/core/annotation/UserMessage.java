package com.spintale.ai.core.annotation;

import java.lang.annotation.*;

/**
 * 用户消息注解
 * 
 * 标记方法参数为用户输入的消息
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface UserMessage {
    
    /**
     * 消息模板（可选）
     * 如果提供，会将参数值填充到模板中
     */
    String value() default "";
}
