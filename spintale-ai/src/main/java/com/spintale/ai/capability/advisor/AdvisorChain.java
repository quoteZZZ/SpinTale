package com.spintale.ai.capability.advisor;

import com.spintale.ai.observability.AiMetricsCollector;
import com.spintale.ai.core.model.ChatMessage;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.output.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Advisor 链执行器
 *
 * 核心执行流程：
 * 1. 请求阶段：按 order 升序依次执行所有 Advisor 的 adviseRequest
 * 2. 调用 LLM 生成回复
 * 3. 响应阶段：按 order 降序依次执行所有 Advisor 的 adviseResponse
 *
 * 参考 Spring AI 的 AdvisorChain 设计，但简化了实现以避免过度抽象
 */
public class AdvisorChain {

    private static final Logger log = LoggerFactory.getLogger(AdvisorChain.class);

    private final List<Advisor> advisors;
    private final ChatModel chatModel;
    private final AiMetricsCollector metricsCollector;

    public AdvisorChain(ChatModel chatModel, List<Advisor> advisors) {
        this(chatModel, advisors, null);
    }

    public AdvisorChain(ChatModel chatModel, List<Advisor> advisors, AiMetricsCollector metricsCollector) {
        this.chatModel = chatModel;
        // 按 order 排序
        this.advisors = new ArrayList<>(advisors);
        this.advisors.sort(Comparator.comparingInt(Advisor::getOrder));
        this.metricsCollector = metricsCollector;
    }

    /**
     * 执行完整的 Advisor 链 + LLM 调用
     */
    public AdvisorResponse execute(AdvisorRequest request) {
        AdvisorContext context = new AdvisorContext();
        long startTime = System.currentTimeMillis();

        // 保存原始查询
        context.put(AdvisorContext.ORIGINAL_QUERY, request.getUserMessage());

        // ==================== 请求阶段（升序执行） ====================
        AdvisorRequest currentRequest = request;
        for (Advisor advisor : advisors) {
            try {
                log.debug("Advisor [{}] processing request", advisor.getName());
                currentRequest = advisor.adviseRequest(currentRequest, context);

                // 检查是否有缓存命中
                if (Boolean.TRUE.equals(context.get(AdvisorContext.CACHE_HIT, Boolean.class))) {
                    String cachedResponse = context.get(AdvisorContext.CACHE_RESPONSE, String.class);
                    if (cachedResponse != null) {
                        log.info("Cache hit, skipping LLM call and remaining advisors");
                        return AdvisorResponse.of(cachedResponse, "cache", null, currentRequest.getSessionId());
                    }
                }
            } catch (Exception e) {
                log.error("Advisor [{}] failed on request phase: {}", advisor.getName(), e.getMessage(), e);
                // 单个 Advisor 失败不中断整个链（避免单点故障）
            }
        }

        // ==================== 调用 LLM ====================
        AdvisorResponse response = callLlm(currentRequest, context);

        // ==================== 响应阶段（降序执行） ====================
        List<Advisor> reversedAdvisors = new ArrayList<>(this.advisors);
        reversedAdvisors.sort(Comparator.comparingInt(Advisor::getOrder).reversed());

        for (Advisor advisor : reversedAdvisors) {
            try {
                log.debug("Advisor [{}] processing response", advisor.getName());
                response = advisor.adviseResponse(response, context);
            } catch (Exception e) {
                log.error("Advisor [{}] failed on response phase: {}", advisor.getName(), e.getMessage(), e);
            }
        }

        // 记录总耗时
        long duration = System.currentTimeMillis() - startTime;
        context.put(AdvisorContext.EXECUTION_DURATION_MS, duration);
        response.setMetadata(AdvisorContext.EXECUTION_DURATION_MS, duration);

        log.info("AdvisorChain completed: duration={}ms, advisors={}", duration, advisors.size());
        return response;
    }

    /**
     * 调用 LLM 生成回复
     */
    private AdvisorResponse callLlm(AdvisorRequest request, AdvisorContext context) {
        // 开始指标收集
        AiMetricsCollector.CallContext callContext = null;
        if (metricsCollector != null) {
            String modelName = getModelName();
            callContext = metricsCollector.startCall(modelName, "chat");
        }

        try {
            // 构建 LangChain4j 消息列表
            List<dev.langchain4j.data.message.ChatMessage> messages = new ArrayList<>();

            // 系统提示词
            if (request.getSystemPrompt() != null && !request.getSystemPrompt().isEmpty()) {
                messages.add(new SystemMessage(request.getSystemPrompt()));
            }

            // 历史消息
            if (request.getHistory() != null) {
                for (ChatMessage histMsg : request.getHistory()) {
                    if ("user".equals(histMsg.getRole())) {
                        messages.add(new UserMessage(histMsg.getContent()));
                    } else if ("assistant".equals(histMsg.getRole())) {
                        messages.add(new AiMessage(histMsg.getContent()));
                    }
                }
            }

            // 当前用户消息
            messages.add(new UserMessage(request.getUserMessage()));

            // 调用模型
            dev.langchain4j.model.chat.response.ChatResponse chatResp = chatModel.chat(messages);
            String responseText = chatResp.aiMessage().text();
            
            // 构建 AdvisorResponse
            AdvisorResponse response = new AdvisorResponse();
            response.setContent(responseText);
            response.setSessionId(request.getSessionId());
            response.setFinished(true);

            // Token 统计（简化版本，实际应从 chatModel 获取）
            // TODO: 如果需要精确的 Token 统计，需要使用 LangChain4j 的 Response<AiMessage>
            int promptTokens = 0;
            int completionTokens = 0;

            // 记录成功指标
            if (callContext != null) {
                callContext.recordSuccess(promptTokens, completionTokens);
            }

            return response;

        } catch (Exception e) {
            log.error("LLM call failed: {}", e.getMessage(), e);
            
            // 记录失败指标
            if (callContext != null) {
                callContext.recordFailure(e);
            }
            
            AdvisorResponse errorResponse = new AdvisorResponse();
            errorResponse.setContent("抱歉，AI 服务暂时不可用：" + e.getMessage());
            errorResponse.setFinished(true);
            errorResponse.setConfidenceScore(0.0);
            return errorResponse;
        }
    }

    /**
     * 获取模型名称（用于指标）
     */
    private String getModelName() {
        try {
            // 尝试从 chatModel 获取模型名称
            return chatModel.getClass().getSimpleName();
        } catch (Exception e) {
            return "unknown";
        }
    }

    /**
     * 获取已注册的 Advisor 列表
     */
    public List<Advisor> getAdvisors() {
        return new ArrayList<>(advisors);
    }
}
