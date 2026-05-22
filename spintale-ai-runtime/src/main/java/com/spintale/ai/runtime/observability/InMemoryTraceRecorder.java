package com.spintale.ai.runtime.observability;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Component;

@Component
public class InMemoryTraceRecorder implements TraceRecorder
{
    private final Map<String, List<AiSpan>> traceSpans = new ConcurrentHashMap<>();
    private final Map<String, AiSpan> allSpans = new ConcurrentHashMap<>();
    private final Map<String, String> traceNames = new ConcurrentHashMap<>();
    private final Map<String, Instant> traceStartTimes = new ConcurrentHashMap<>();

    @Override
    public String startTrace(String name)
    {
        String traceId = java.util.UUID.randomUUID().toString();
        traceNames.put(traceId, name);
        traceStartTimes.put(traceId, Instant.now());
        traceSpans.put(traceId, new CopyOnWriteArrayList<>());
        return traceId;
    }

    @Override
    public String startTrace(String name, String parentTraceId)
    {
        return startTrace(name);
    }

    @Override
    public AiSpan startSpan(String traceId, String spanName, AiSpan.SpanKind kind)
    {
        AiSpan span = AiSpan.start(traceId, spanName, kind);
        allSpans.put(span.getSpanId(), span);

        List<AiSpan> spans = traceSpans.computeIfAbsent(traceId, k -> new CopyOnWriteArrayList<>());
        spans.add(span);

        return span;
    }

    @Override
    public void endSpan(AiSpan span)
    {
        span.end();
    }

    @Override
    public void endSpanWithError(AiSpan span, String error)
    {
        span.endWithError(error);
    }

    @Override
    public void addSpanEvent(AiSpan span, String eventName, String description)
    {
        span.addEvent(eventName, description);
    }

    @Override
    public void setSpanAttribute(AiSpan span, String key, Object value)
    {
        span.setAttribute(key, value);
    }

    @Override
    public java.util.Optional<AiSpan> getSpan(String spanId)
    {
        return java.util.Optional.ofNullable(allSpans.get(spanId));
    }

    @Override
    public List<AiSpan> getTraceSpans(String traceId)
    {
        return traceSpans.getOrDefault(traceId, List.of());
    }

    @Override
    public TraceSummary getTraceSummary(String traceId)
    {
        List<AiSpan> spans = getTraceSpans(traceId);
        if (spans.isEmpty())
        {
            return null;
        }

        Instant startTime = traceStartTimes.get(traceId);
        Instant endTime = spans.stream()
                .filter(s -> s.getEndTime() != null)
                .map(AiSpan::getEndTime)
                .max(Instant::compareTo)
                .orElse(null);

        long totalDuration = 0;
        if (startTime != null && endTime != null)
        {
            totalDuration = ChronoUnit.MILLIS.between(startTime, endTime);
        }

        int errorCount = (int) spans.stream()
                .filter(s -> s.getStatus() == AiSpan.SpanStatus.ERROR)
                .count();

        String status = errorCount > 0 ? "ERROR" : "OK";

        return new TraceSummary(
                traceId,
                traceNames.getOrDefault(traceId, "unknown"),
                startTime,
                endTime,
                totalDuration,
                spans.size(),
                errorCount,
                status
        );
    }

    @Override
    public void recordException(String traceId, String spanId, Throwable exception)
    {
        AiSpan span = allSpans.get(spanId);
        if (span != null)
        {
            span.endWithError(exception.getMessage());
            span.setAttribute("exception.type", exception.getClass().getName());
            span.setAttribute("exception.message", exception.getMessage());
        }
    }

    public void clear()
    {
        traceSpans.clear();
        allSpans.clear();
        traceNames.clear();
        traceStartTimes.clear();
    }
}
