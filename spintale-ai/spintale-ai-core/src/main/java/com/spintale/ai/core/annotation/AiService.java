package com.spintale.ai.core.annotation;

import java.lang.annotation.*;

/**
 * 声明式AI服务接口标记
 * 
 * 灵感来源: Quarkus LangChain4j @RegisterAiService
 * 
 * 使用示例:
 * <pre>{@code
 * @AiService
 * public interface CustomerSupport {
 *     
 *     @SystemMessage("你是专业的客服助手")
 *     String answerQuestion(@UserMessage String question);
 *     
 *     @SystemMessage("你是翻译专家")
 *     @Temperature(0.3)
 *     String translate(@UserMessage String text, @Language("zh") String targetLang);
 * }
 * }</pre>
 * 
 * @author SpinTale AI Team
 * @since 1.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AiService {
    
    /**
     * AI服务名称（用于日志和监控）
     */
    String name() default "";
    
    /**
     * 使用的模型名称
     * 为空则使用默认模型
     */
    String model() default "";
    
    /**
     * 是否启用记忆功能
     */
    boolean memoryEnabled() default true;
    
    /**
     * 最大对话历史消息数
     */
    int maxMessages() default 20;
    
    /**
     * 超时时间（毫秒）
     */
    long timeout() default 60000;
}
