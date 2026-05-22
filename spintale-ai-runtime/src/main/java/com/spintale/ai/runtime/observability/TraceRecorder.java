package com.spintale.ai.runtime.observability;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface TraceRecorder
{
    String startTrace(String name);

    String startTrace(String name, String parentTraceId);

    AiSpan startSpan(String traceId, String spanName, AiSpan.SpanKind kind);

    void endSpan(AiSpan span);

    void endSpanWithError(AiSpan span, String error);

    void addSpanEvent(AiSpan span, String eventName, String description);

    void setSpanAttribute(AiSpan span, String key, Object value);

    Optional<AiSpan> getSpan(String spanId);

    List<AiSpan> getTraceSpans(String traceId);

    TraceSummary getTraceSummary(String traceId);

    void recordException(String traceId, String spanId, Throwable exception);

    record TraceSummary(
            String traceId,
            String name,
            Instant startTime,
            Instant endTime,
            long totalDurationMs,
            int spanCount,
            int errorCount,
            String status
    ) {}
}
