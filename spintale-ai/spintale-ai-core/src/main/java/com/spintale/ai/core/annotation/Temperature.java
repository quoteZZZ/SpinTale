package com.spintale.ai.core.annotation;

import java.lang.annotation.*;

/**
 * 温度参数注解
 * 
 * 控制AI输出的随机性
 * 0.0 = 确定性最高，1.0 = 最随机
 */
@Target({ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Temperature {
    
    /**
     * 温度值 (0.0 - 2.0)
     */
    double value();
}
