package com.spintale.ai.runtime.facade;

/**
 * AI服务接口
 * 定义AI服务的标准操作
 */
public interface AiServiceFacade {

    /**
     * 简单对话
     *
     * @param message 用户消息
     * @return AI响应
     */
    String chat(String message);

    /**
     * 带系统提示的对话
     *
     * @param systemMessage 系统提示
     * @param userMessage 用户消息
     * @return AI响应
     */
    String chat(String systemMessage, String userMessage);

    /**
     * 检查服务是否可用
     *
     * @return true表示服务可用
     */
    boolean isAvailable();

    /**
     * 获取服务名称
     *
     * @return 服务名称
     */
    String getServiceName();
}
