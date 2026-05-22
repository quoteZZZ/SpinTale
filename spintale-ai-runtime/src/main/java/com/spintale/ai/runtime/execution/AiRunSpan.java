package com.spintale.ai.runtime.execution;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Data;
import lombok.Getter;

@Data
public class AiRunSpan
{
    private String spanId;
    private String parentSpanId;
    private String runId;
    private String traceId;
    private String spanName;
    private SpanType spanType;
    private Instant startTime;
    private Instant endTime;
    private Long durationMs;
    private Map<String, Object> attributes;
    private List<AiRunSpan> childSpans;
    private String status;

    public enum SpanType
    {
        CHAT,
        EMBEDDING,
        RERANK,
        RETRIEVAL,
        TOOL_CALL,
        AGENT_STEP,
        WORKFLOW_STEP,
        RAG_QUERY,
        RAG_RETRIEVE,
        RAG_RERANK,
        RAG_GENERATE
    }

    public static AiRunSpan start(String runId, String traceId, 
            String spanName, SpanType type)
    {
        AiRunSpan span = new AiRunSpan();
        span.setSpanId(java.util.UUID.randomUUID().toString());
        span.setRunId(runId);
        span.setTraceId(traceId);
        span.setSpanName(spanName);
        span.setSpanType(type);
        span.setStartTime(Instant.now());
        span.setAttributes(new ConcurrentHashMap<>());
        span.setChildSpans(new ArrayList<>());
        span.setStatus("RUNNING");
        return span;
    }

    public void end()
    {
        this.endTime = Instant.now();
        this.durationMs = endTime.toEpochMilli() - startTime.toEpochMilli();
        this.status = "COMPLETED";
    }

    public void endWithError()
    {
        this.endTime = Instant.now();
        this.durationMs = endTime.toEpochMilli() - startTime.toEpochMilli();
        this.status = "ERROR";
    }

    public void setAttribute(String key, Object value)
    {
        if (this.attributes == null)
        {
            this.attributes = new ConcurrentHashMap<>();
        }
        this.attributes.put(key, value);
    }

    public void addChildSpan(AiRunSpan child)
    {
        if (this.childSpans == null)
        {
            this.childSpans = new ArrayList<>();
        }
        child.setParentSpanId(this.spanId);
        this.childSpans.add(child);
    }
}
