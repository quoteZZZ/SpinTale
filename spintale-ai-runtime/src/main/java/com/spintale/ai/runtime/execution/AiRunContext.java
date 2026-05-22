package com.spintale.ai.runtime.execution;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Data
@Builder
public class AiRunContext
{
    private String runId;
    private String traceId;
    private String parentSpanId;
    private String runType;
    private String model;
    private String provider;
    private String userId;
    private String sessionId;
    private String knowledgeBaseId;
    private String agentId;
    private Map<String, Object> attributes;
    private Instant startTime;
    private Instant endTime;
    private RunStatus status;
    private long timeoutMs;
    private int retryCount;
    private int maxRetries;

    public enum RunStatus
    {
        PENDING,
        RUNNING,
        SUCCEEDED,
        FAILED,
        TIMEOUT,
        CANCELLED,
        RETRYING
    }

    public static AiRunContext create()
    {
        return AiRunContext.builder()
                .runId(UUID.randomUUID().toString())
                .traceId(UUID.randomUUID().toString())
                .attributes(new HashMap<>())
                .startTime(Instant.now())
                .status(RunStatus.PENDING)
                .retryCount(0)
                .maxRetries(3)
                .timeoutMs(30000L)
                .build();
    }

    public static AiRunContext create(String runType)
    {
        AiRunContext ctx = create();
        ctx.setRunType(runType);
        return ctx;
    }

    public AiRunContext withModel(String model)
    {
        this.model = model;
        return this;
    }

    public AiRunContext withProvider(String provider)
    {
        this.provider = provider;
        return this;
    }

    public AiRunContext withUser(String userId)
    {
        this.userId = userId;
        return this;
    }

    public AiRunContext withSession(String sessionId)
    {
        this.sessionId = sessionId;
        return this;
    }

    public AiRunContext withAgent(String agentId)
    {
        this.agentId = agentId;
        return this;
    }

    public AiRunContext withKnowledgeBase(String kbId)
    {
        this.knowledgeBaseId = kbId;
        return this;
    }

    public AiRunContext withTimeout(long timeoutMs)
    {
        this.timeoutMs = timeoutMs;
        return this;
    }

    public AiRunContext withMaxRetries(int maxRetries)
    {
        this.maxRetries = maxRetries;
        return this;
    }

    public AiRunContext setAttribute(String key, Object value)
    {
        if (this.attributes == null)
        {
            this.attributes = new HashMap<>();
        }
        this.attributes.put(key, value);
        return this;
    }

    public Object getAttribute(String key)
    {
        return this.attributes != null ? this.attributes.get(key) : null;
    }

    public void markRunning()
    {
        this.status = RunStatus.RUNNING;
        this.startTime = Instant.now();
    }

    public void markSucceeded()
    {
        this.status = RunStatus.SUCCEEDED;
        this.endTime = Instant.now();
    }

    public void markFailed()
    {
        this.status = RunStatus.FAILED;
        this.endTime = Instant.now();
    }

    public void markTimeout()
    {
        this.status = RunStatus.TIMEOUT;
        this.endTime = Instant.now();
    }

    public void markRetrying()
    {
        this.status = RunStatus.RETRYING;
        this.retryCount++;
    }

    public long getDurationMs()
    {
        if (startTime == null) return 0;
        Instant end = endTime != null ? endTime : Instant.now();
        return end.toEpochMilli() - startTime.toEpochMilli();
    }

    public boolean isFinished()
    {
        return status == RunStatus.SUCCEEDED 
            || status == RunStatus.FAILED 
            || status == RunStatus.TIMEOUT 
            || status == RunStatus.CANCELLED;
    }

    public boolean canRetry()
    {
        return retryCount < maxRetries && status == RunStatus.RETRYING;
    }
}
