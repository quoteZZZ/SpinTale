package com.spintale.ai.core.annotation;

import java.lang.annotation.*;

/**
 * 语言参数注解
 * 
 * 用于指定目标语言
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Language {
    
    /**
     * 语言代码（如: zh, en, ja, fr）
     */
    String value() default "";
}
