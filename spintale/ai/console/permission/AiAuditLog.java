package com.spintale.ai.console.permission;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AiAuditLog
{
    String module() default "AI";

    String operation();

    String description() default "";

    boolean recordParams() default true;

    boolean recordResult() default false;
}
