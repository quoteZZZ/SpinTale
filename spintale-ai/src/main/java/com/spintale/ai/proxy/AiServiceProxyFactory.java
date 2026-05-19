package com.spintale.ai.proxy;

import com.spintale.ai.core.annotation.*;
import com.spintale.ai.infrastructure.client.ChatClient;
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
 * 声明式AI服务代理工厂
 * 
 * 灵感来源: Quarkus LangChain4j + Spring AI
 * 
 * 功能:
 * 1. 自动为@AiService接口创建代理实现
 * 2. 解析方法注解配置（SystemMessage, Temperature等）
 * 3. 调用底层ChatClient执行AI请求
 * 
 * @author SpinTale AI Team
 */
@Slf4j
public class AiServiceProxyFactory implements FactoryBean<Object>, ApplicationContextAware {
    
    private final Class<?> serviceInterface;
    private ApplicationContext applicationContext;
    
    public AiServiceProxyFactory(Class<?> serviceInterface) {
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
    
    /**
     * AI服务调用处理器
     */
    private class AiServiceInvocationHandler implements InvocationHandler {
        
        private final ChatClient chatClient;
        
        public AiServiceInvocationHandler() {
            // 从Spring容器获取ChatClient
            this.chatClient = applicationContext.getBean(ChatClient.class);
        }
        
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            // 处理Object类的方法
            if (method.getDeclaringClass() == Object.class) {
                return method.invoke(this, args);
            }
            
            // 获取接口级别的@AiService配置
            AiService aiService = serviceInterface.getAnnotation(AiService.class);
            
            // 构建聊天请求
            Map<String, Object> config = buildConfig(method, aiService);
            
            // 提取用户消息
            String userMessage = extractUserMessage(method, args);
            
            // 提取系统消息
            String systemMessage = extractSystemMessage(method, aiService);
            
            log.debug("Executing AI service: {}.{} with model: {}", 
                serviceInterface.getSimpleName(), 
                method.getName(),
                config.get("model"));
            
            // 调用ChatClient
            return chatClient.prompt()
                .user(userMessage)
                .system(systemMessage)
                .temperature((Double) config.getOrDefault("temperature", 0.7))
                .call()
                .content();
        }
        
        /**
         * 构建配置
         */
        private Map<String, Object> buildConfig(Method method, AiService aiService) {
            Map<String, Object> config = new HashMap<>();
            
            // 接口级别配置
            if (aiService != null) {
                config.put("model", aiService.model());
                config.put("timeout", aiService.timeout());
            }
            
            // 方法级别配置（覆盖接口配置）
            Temperature tempAnnotation = method.getAnnotation(Temperature.class);
            if (tempAnnotation != null) {
                config.put("temperature", tempAnnotation.value());
            }
            
            return config;
        }
        
        /**
         * 提取用户消息
         */
        private String extractUserMessage(Method method, Object[] args) {
            // 查找@UserMessage参数
            for (int i = 0; i < method.getParameterCount(); i++) {
                if (method.getParameterAnnotations()[i].length > 0) {
                    for (var annotation : method.getParameterAnnotations()[i]) {
                        if (annotation instanceof UserMessage) {
                            UserMessage userMsg = (UserMessage) annotation;
                            String template = userMsg.value();
                            
                            if (template.isEmpty()) {
                                // 直接使用参数值
                                return args[i] != null ? args[i].toString() : "";
                            } else {
                                // 使用模板
                                return template.replace("{0}", args[i] != null ? args[i].toString() : "");
                            }
                        }
                    }
                }
            }
            
            // 如果没有@UserMessage，使用第一个String参数
            if (args != null && args.length > 0 && args[0] instanceof String) {
                return (String) args[0];
            }
            
            throw new IllegalArgumentException("No user message found in method parameters");
        }
        
        /**
         * 提取系统消息
         */
        private String extractSystemMessage(Method method, AiService aiService) {
            // 方法级别的@SystemMessage优先
            SystemMessage methodSystemMsg = method.getAnnotation(SystemMessage.class);
            if (methodSystemMsg != null) {
                return methodSystemMsg.value();
            }
            
            // 其次使用接口级别的@SystemMessage
            SystemMessage interfaceSystemMsg = serviceInterface.getAnnotation(SystemMessage.class);
            if (interfaceSystemMsg != null) {
                return interfaceSystemMsg.value();
            }
            
            // 默认系统消息
            return "You are a helpful assistant.";
        }
    }
}
