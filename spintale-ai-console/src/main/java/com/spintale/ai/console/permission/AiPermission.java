package com.spintale.ai.console.permission;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AiPermission
{
    String value();

    String description() default "";
}
