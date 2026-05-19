package com.spintale.ai.capability.advisor;

/**
 * AI 顾问拦截器接口（参考 Spring AI Advisor 模式）
 *
 * 提供可组合的请求/响应拦截链，实现横切关注点的模块化：
 * - 记忆注入/保存
 * - RAG 上下文增强
 * - 安全围栏检测
 * - 幻觉检测与缓解
 * - 日志与指标采集
 *
 * 设计原则：
 * 1. 单一职责 - 每个 Advisor 只做一件事
 * 2. 有序执行 - 通过 order 控制执行顺序
 * 3. 可中断 - 可选择跳过后续处理
 * 4. 上下文共享 - 通过 AdvisorContext 在链中传递数据
 */
public interface Advisor {

    /**
     * 获取顾问名称
     */
    String getName();

    /**
     * 执行顺序，数值越小越先执行
     * 请求阶段按升序执行，响应阶段按降序执行
     *
     * 建议顺序：
     * - 0-99:   安全与围栏（最外层拦截）
     * - 100-199: 日志与指标
     * - 200-299: 语义缓存
     * - 300-399: 记忆注入
     * - 400-499: RAG 上下文增强
     * - 500-599: 模型路由
     * - 600-699: 幻觉检测
     * - 700-799: 输出过滤与围栏
     */
    int getOrder();

    /**
     * 请求拦截：在发送给 LLM 之前处理请求
     *
     * @param request 原始请求
     * @param context 顾问上下文（可跨 Advisor 共享数据）
     * @return 处理后的请求（可修改）
     */
    AdvisorRequest adviseRequest(AdvisorRequest request, AdvisorContext context);

    /**
     * 响应拦截：在 LLM 返回结果后处理响应
     *
     * @param response 原始响应
     * @param context 顾问上下文
     * @return 处理后的响应（可修改）
     */
    AdvisorResponse adviseResponse(AdvisorResponse response, AdvisorContext context);
}
