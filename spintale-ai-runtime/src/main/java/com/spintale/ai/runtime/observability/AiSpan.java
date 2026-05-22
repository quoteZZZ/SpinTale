package com.spintale.ai.runtime.observability;

import java.time.Instant;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AiSpan
{
    private String spanId;
    private String traceId;
    private String parentSpanId;
    private String name;
    private SpanKind kind;
    private Instant startTime;
    private Instant endTime;
    private long durationMs;
    private SpanStatus status;
    private Map<String, Object> attributes;
    private Map<String, String> events;
    private String errorMessage;

    public enum SpanKind
    {
        INTERNAL,
        CLIENT,
        SERVER,
        PRODUCER,
        CONSUMER
    }

    public enum SpanStatus
    {
        UNSET,
        OK,
        ERROR
    }

    public boolean isEnded()
    {
        return endTime != null;
    }

    public void end()
    {
        this.endTime = Instant.now();
        if (startTime != null)
        {
            this.durationMs = endTime.toEpochMilli() - startTime.toEpochMilli();
        }
        this.status = SpanStatus.OK;
    }

    public void endWithError(String error)
    {
        this.endTime = Instant.now();
        if (startTime != null)
        {
            this.durationMs = endTime.toEpochMilli() - startTime.toEpochMilli();
        }
        this.status = SpanStatus.ERROR;
        this.errorMessage = error;
    }

    public void setAttribute(String key, Object value)
    {
        if (this.attributes == null)
        {
            this.attributes = new java.util.HashMap<>();
        }
        this.attributes.put(key, value);
    }

    public void addEvent(String name, String description)
    {
        if (this.events == null)
        {
            this.events = new java.util.HashMap<>();
        }
        this.events.put(name, description);
    }

    public static AiSpan start(String traceId, String name, SpanKind kind)
    {
        return AiSpan.builder()
                .spanId(java.util.UUID.randomUUID().toString())
                .traceId(traceId)
                .name(name)
                .kind(kind)
                .startTime(Instant.now())
                .status(SpanStatus.UNSET)
                .attributes(new java.util.HashMap<>())
                .events(new java.util.HashMap<>())
                .build();
    }

    public static AiSpan startInternal(String traceId, String name)
    {
        return start(traceId, name, SpanKind.INTERNAL);
    }

    public static AiSpan startClient(String traceId, String name)
    {
        return start(traceId, name, SpanKind.CLIENT);
    }
}
