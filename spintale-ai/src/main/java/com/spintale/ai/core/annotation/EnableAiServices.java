package com.spintale.ai.core.annotation;

import com.spintale.ai.infrastructure.proxy.AiServiceRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Enables scanning for interfaces annotated with {@link AiService}.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(AiServiceRegistrar.class)
public @interface EnableAiServices {

    String[] basePackages() default {};

    Class<?>[] basePackageClasses() default {};
}
