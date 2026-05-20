package com.spintale.ai.agent.tool.annotation;

import com.spintale.ai.agent.tool.registry.ToolRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Component
public class ToolAnnotationProcessor implements BeanPostProcessor {
    
    private static final Logger log = LoggerFactory.getLogger(ToolAnnotationProcessor.class);
    
    private final ToolRegistry toolRegistry;
    
    public ToolAnnotationProcessor(ToolRegistry toolRegistry) {
        this.toolRegistry = toolRegistry;
    }
    
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        ReflectionUtils.doWithMethods(bean.getClass(), method -> {
            Tool toolAnnotation = method.getAnnotation(Tool.class);
            if (toolAnnotation != null && toolAnnotation.enabled()) {
                registerTool(bean, method, toolAnnotation);
            }
        });
        return bean;
    }
    
    private void registerTool(Object bean, Method method, Tool annotation) {
        String toolName = annotation.name().isEmpty() ? method.getName() : annotation.name();
        String description = annotation.description();
        
        Method finalMethod = method;
        toolRegistry.register(toolName, description, args -> {
            try {
                Object[] params = buildParameters(finalMethod, args);
                Object result = finalMethod.invoke(bean, params);
                return result != null ? String.valueOf(result) : "null";
            } catch (Exception e) {
                log.error("Tool execution failed: {}", toolName, e);
                return "Error: " + e.getMessage();
            }
        });
        
        log.info("Registered tool via @Tool annotation: {} - {}", toolName, description);
    }
    
    private Object[] buildParameters(Method method, Map<String, Object> args) {
        java.lang.reflect.Parameter[] parameters = method.getParameters();
        Object[] params = new Object[parameters.length];
        
        for (int i = 0; i < parameters.length; i++) {
            java.lang.reflect.Parameter param = parameters[i];
            ToolParam toolParam = param.getAnnotation(ToolParam.class);
            
            String paramName = toolParam != null && !toolParam.value().isEmpty() 
                    ? toolParam.value() 
                    : param.getName();
            
            Object value = args != null ? args.get(paramName) : null;
            
            if (value == null && toolParam != null && !toolParam.defaultValue().isEmpty()) {
                value = convertValue(toolParam.defaultValue(), param.getType());
            }
            
            if (value == null && toolParam != null && toolParam.required()) {
                throw new IllegalArgumentException("Required parameter '" + paramName + "' is missing");
            }
            
            params[i] = value != null ? convertValue(value, param.getType()) : null;
        }
        
        return params;
    }
    
    private Object convertValue(Object value, Class<?> targetType) {
        if (value == null) {
            return null;
        }
        
        if (targetType.isInstance(value)) {
            return value;
        }
        
        String strValue = String.valueOf(value);
        
        if (targetType == String.class) {
            return strValue;
        } else if (targetType == Integer.class || targetType == int.class) {
            return Integer.parseInt(strValue);
        } else if (targetType == Long.class || targetType == long.class) {
            return Long.parseLong(strValue);
        } else if (targetType == Double.class || targetType == double.class) {
            return Double.parseDouble(strValue);
        } else if (targetType == Boolean.class || targetType == boolean.class) {
            return Boolean.parseBoolean(strValue);
        }
        
        return value;
    }
}
