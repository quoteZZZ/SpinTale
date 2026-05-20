package com.spintale.ai.agent.tool.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Tool {
    
    String name() default "";
    
    String description();
    
    boolean enabled() default true;
    
    long timeoutMs() default 30000;
    
    String[] tags() default {};
}
