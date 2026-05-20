package com.spintale.ai.agent.tool.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface ToolParam {
    
    String value() default "";
    
    String description() default "";
    
    boolean required() default true;
    
    String defaultValue() default "";
}
