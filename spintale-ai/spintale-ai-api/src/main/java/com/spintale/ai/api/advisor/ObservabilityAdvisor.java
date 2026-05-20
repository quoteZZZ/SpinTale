package com.spintale.ai.api.advisor;

import com.spintale.ai.core.metrics.CostMonitor;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * 可观测�?Advisor
 *
 * 采集 AI 操作的关键指标：
 * - 请求计数
 * - 响应时间分布
 * - Token 消�?
 * - 错误�?
 * - 幻觉检测统�?
 *
 * 参�?Spring AI �?Observation 架构，但使用更轻量的 Micrometer 直接集成
 */
public class ObservabilityAdvisor implements Advisor {

    private static final Logger log = LoggerFactory.getLogger(ObservabilityAdvisor.class);

    private final MeterRegistry meterRegistry;
    private final CostMonitor costMonitor;

    // 指标
    private Counter requestCounter;
    private Counter errorCounter;
    private Counter hallucinationCounter;
    private Timer responseTimer;
    private io.micrometer.core.instrument.DistributionSummary tokenInputSummary;
    private io.micrometer.core.instrument.DistributionSummary tokenOutputSummary;

    public ObservabilityAdvisor(MeterRegistry meterRegistry) {
        this(meterRegistry, null);
    }

    public ObservabilityAdvisor(MeterRegistry meterRegistry, CostMonitor costMonitor) {
        this.meterRegistry = meterRegistry;
        this.costMonitor = costMonitor;
        initMetrics();
    }

    private void initMetrics() {
        if (meterRegistry == null) {
            return;
        }

        this.requestCounter = Counter.builder("spintale.ai.requests.total")
                .description("Total AI chat requests")
                .register(meterRegistry);

        this.errorCounter = Counter.builder("spintale.ai.requests.errors")
                .description("AI chat request errors")
                .register(meterRegistry);

        this.hallucinationCounter = Counter.builder("spintale.ai.hallucination.detected")
                .description("Hallucination detection count")
                .register(meterRegistry);

        this.responseTimer = Timer.builder("spintale.ai.requests.duration")
                .description("AI chat request duration")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry);

        this.tokenInputSummary = io.micrometer.core.instrument.DistributionSummary
                .builder("spintale.ai.tokens.input")
                .description("Input token usage")
                .register(meterRegistry);

        this.tokenOutputSummary = io.micrometer.core.instrument.DistributionSummary
                .builder("spintale.ai.tokens.output")
                .description("Output token usage")
                .register(meterRegistry);
    }

    @Override
    public String getName() {
        return "ObservabilityAdvisor";
    }

    @Override
    public int getOrder() {
        return AdvisorOrder.OBSERVABILITY;�?
    }

    @Override
    public AdvisorRequest adviseRequest(AdvisorRequest request, AdvisorContext context) {
        // 记录请求开始时�?
        context.put("request_start_time", System.nanoTime());

        if (requestCounter != null) {
            requestCounter.increment();
        }

        log.info("AI request: userId={}, sessionId={}, messageLength={}",
                request.getUserId(), request.getSessionId(),
                request.getUserMessage() != null ? request.getUserMessage().length() : 0);

        return request;
    }

    @Override
    public AdvisorResponse adviseResponse(AdvisorResponse response, AdvisorContext context) {
        // 计算响应时间
        Long startTime = (Long) context.get("request_start_time");
        if (startTime != null) {
            long durationNanos = System.nanoTime() - startTime;
            long durationMs = TimeUnit.NANOSECONDS.toMillis(durationNanos);

            if (responseTimer != null) {
                responseTimer.record(durationNanos, TimeUnit.NANOSECONDS);
            }

            log.info("AI response: duration={}ms, confidence={}, finished={}",
                    durationMs, response.getConfidenceScore(), response.isFinished());
        }

        // 记录 Token 用量
        if (response.getTokenUsage() != null) {
            if (tokenInputSummary != null && response.getTokenUsage().getPromptTokens() > 0) {
                tokenInputSummary.record(response.getTokenUsage().getPromptTokens());
            }
            if (tokenOutputSummary != null && response.getTokenUsage().getCompletionTokens() > 0) {
                tokenOutputSummary.record(response.getTokenUsage().getCompletionTokens());
            }

            log.info("Token usage: input={}, output={}, total={}",
                    response.getTokenUsage().getPromptTokens(),
                    response.getTokenUsage().getCompletionTokens(),
                    response.getTokenUsage().getTotalTokens());

            if (costMonitor != null && response.getModel() != null && !response.getModel().isBlank()) {
                costMonitor.recordUsage(
                        response.getModel(),
                        response.getTokenUsage().getPromptTokens(),
                        response.getTokenUsage().getCompletionTokens());
            }
        }

        // 记录幻觉检�?
        Double confidence = response.getConfidenceScore();
        if (confidence != null && confidence < 0.5 && hallucinationCounter != null) {
            hallucinationCounter.increment();
        }

        // 记录错误
        if (response.getContent() != null && response.getContent().contains("AI 服务暂时不可�?)) {
            if (errorCounter != null) {
                errorCounter.increment();
            }
        }

        return response;
    }
}
