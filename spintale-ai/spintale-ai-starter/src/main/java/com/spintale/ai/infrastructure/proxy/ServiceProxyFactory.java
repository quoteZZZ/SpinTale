package com.spintale.ai.infrastructure.proxy;

import com.spintale.ai.core.annotation.*;
import com.spintale.ai.core.api.ChatClient;
import com.spintale.ai.core.model.ChatResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

/**
 * Factory for declarative interfaces annotated with {@link AiService}.
 */
@Slf4j
public class ServiceProxyFactory implements FactoryBean<Object>, ApplicationContextAware {
    
    private final Class<?> serviceInterface;
    private ApplicationContext applicationContext;
    
    public ServiceProxyFactory(Class<?> serviceInterface) {
        this.serviceInterface = serviceInterface;
    }
    
    @Override
    public Object getObject() {
        return Proxy.newProxyInstance(
            serviceInterface.getClassLoader(),
            new Class<?>[]{serviceInterface},
            new AiServiceInvocationHandler()
        );
    }
    
    @Override
    public Class<?> getObjectType() {
        return serviceInterface;
    }
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
    
    private class AiServiceInvocationHandler implements InvocationHandler {
        
        private final ChatClient chatClient;
        
        public AiServiceInvocationHandler() {
            this.chatClient = applicationContext.getBean(ChatClient.class);
        }
        
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getDeclaringClass() == Object.class) {
                return method.invoke(this, args);
            }
            
            AiService aiService = serviceInterface.getAnnotation(AiService.class);
            Map<String, Object> config = buildConfig(method, aiService);
            String userMessage = extractUserMessage(method, args);
            String systemMessage = extractSystemMessage(method, aiService);
            
            log.debug("Executing AI service: {}.{} with model: {}", 
                serviceInterface.getSimpleName(), 
                method.getName(),
                config.get("model"));
            
            ChatClient.PromptBuilder builder = chatClient.prompt()
                    .user(userMessage)
                    .system(systemMessage)
                    .temperature((Double) config.getOrDefault("temperature", 0.7));
            Object model = config.get("model");
            if (model instanceof String modelName && !modelName.isBlank()) {
                builder.param("model", modelName);
            }

            ChatResponse response = builder.call().execute();
            if (method.getReturnType() == ChatResponse.class) {
                return response;
            }
            if (method.getReturnType() == Void.TYPE || method.getReturnType() == Void.class) {
                return null;
            }
            if (method.getReturnType() == String.class || method.getReturnType() == Object.class) {
                return response.getContent();
            }
            throw new IllegalStateException("Unsupported AI service return type: " + method.getReturnType().getName());
        }
        
        private Map<String, Object> buildConfig(Method method, AiService aiService) {
            Map<String, Object> config = new HashMap<>();
            
            if (aiService != null) {
                config.put("model", aiService.model());
                config.put("timeout", aiService.timeout());
            }
            
            Temperature tempAnnotation = method.getAnnotation(Temperature.class);
            if (tempAnnotation != null) {
                config.put("temperature", tempAnnotation.value());
            }
            
            return config;
        }
        
        private String extractUserMessage(Method method, Object[] args) {
            for (int i = 0; i < method.getParameterCount(); i++) {
                if (method.getParameterAnnotations()[i].length > 0) {
                    for (var annotation : method.getParameterAnnotations()[i]) {
                        if (annotation instanceof UserMessage) {
                            UserMessage userMsg = (UserMessage) annotation;
                            String template = userMsg.value();
                            
                            if (template.isEmpty()) {
                                return args[i] != null ? args[i].toString() : "";
                            } else {
                                return template.replace("{0}", args[i] != null ? args[i].toString() : "");
                            }
                        }
                    }
                }
            }
            
            if (args != null && args.length > 0 && args[0] instanceof String) {
                return (String) args[0];
            }
            
            throw new IllegalArgumentException("No user message found in method parameters");
        }
        
        private String extractSystemMessage(Method method, AiService aiService) {
            SystemMessage methodSystemMsg = method.getAnnotation(SystemMessage.class);
            if (methodSystemMsg != null) {
                return methodSystemMsg.value();
            }
            
            SystemMessage interfaceSystemMsg = serviceInterface.getAnnotation(SystemMessage.class);
            if (interfaceSystemMsg != null) {
                return interfaceSystemMsg.value();
            }
            
            return "You are a helpful assistant.";
        }
    }
}
