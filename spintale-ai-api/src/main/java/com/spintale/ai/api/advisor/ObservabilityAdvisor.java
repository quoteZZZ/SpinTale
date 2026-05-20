package com.spintale.ai.api.advisor;

import com.spintale.ai.core.metrics.CostMonitor;

import io.micrometer.core.instrument.MeterRegistry;

/**
 * Records basic advisor-chain metrics.
 */
public class ObservabilityAdvisor implements Advisor {

    private final MeterRegistry meterRegistry;
    private final CostMonitor costMonitor;

    public ObservabilityAdvisor(MeterRegistry meterRegistry, CostMonitor costMonitor) {
        this.meterRegistry = meterRegistry;
        this.costMonitor = costMonitor;
    }

    @Override
    public String getName() {
        return "ObservabilityAdvisor";
    }

    @Override
    public int getOrder() {
        return AdvisorOrder.OBSERVABILITY;
    }

    @Override
    public AdvisorRequest adviseRequest(AdvisorRequest request, AdvisorContext context) {
        context.put(AdvisorContext.ORIGINAL_QUERY, request == null ? null : request.getUserMessage());
        return request;
    }

    @Override
    public AdvisorResponse adviseResponse(AdvisorResponse response, AdvisorContext context) {
        if (meterRegistry != null) {
            meterRegistry.counter("spintale.ai.chat.responses").increment();
            meterRegistry.timer("spintale.ai.chat.latency").record(
                    context.getElapsedTime(), java.util.concurrent.TimeUnit.MILLISECONDS);
        }
        if (costMonitor != null && response != null && response.getTokenUsage() != null) {
            costMonitor.recordUsage(
                    response.getModel(),
                    response.getTokenUsage().getPromptTokens(),
                    response.getTokenUsage().getCompletionTokens());
        }
        return response;
    }
}
