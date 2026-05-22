package com.spintale.ai.agent.run;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AgentRun
{
    private String runId;
    private String agentId;
    private String agentName;
    private String userId;
    private String sessionId;
    private String model;
    private RunStatus status;
    private Instant startTime;
    private Instant endTime;
    private Long durationMs;
    private String input;
    private String output;
    private List<AgentStep> steps;
    private Map<String, Object> metadata;
    private Long totalInputTokens;
    private Long totalOutputTokens;
    private Double totalCost;
    private String errorMessage;

    public enum RunStatus
    {
        PENDING,
        RUNNING,
        SUCCEEDED,
        FAILED,
        CANCELLED,
        TIMEOUT,
        AWAITING_APPROVAL
    }

    public void addStep(AgentStep step)
    {
        if (this.steps == null)
        {
            this.steps = new ArrayList<>();
        }
        this.steps.add(step);
    }

    public AgentStep currentStep()
    {
        if (steps == null || steps.isEmpty())
        {
            return null;
        }
        return steps.get(steps.size() - 1);
    }

    public int getStepCount()
    {
        return steps != null ? steps.size() : 0;
    }

    public boolean isFinished()
    {
        return status == RunStatus.SUCCEEDED 
            || status == RunStatus.FAILED 
            || status == RunStatus.CANCELLED
            || status == RunStatus.TIMEOUT;
    }

    public boolean isAwaitingApproval()
    {
        return status == RunStatus.AWAITING_APPROVAL;
    }

    public void markSucceeded(String output)
    {
        this.status = RunStatus.SUCCEEDED;
        this.output = output;
        this.endTime = Instant.now();
        if (startTime != null)
        {
            this.durationMs = endTime.toEpochMilli() - startTime.toEpochMilli();
        }
    }

    public void markFailed(String error)
    {
        this.status = RunStatus.FAILED;
        this.errorMessage = error;
        this.endTime = Instant.now();
        if (startTime != null)
        {
            this.durationMs = endTime.toEpochMilli() - startTime.toEpochMilli();
        }
    }

    public void accumulateTokens(Long inputTokens, Long outputTokens)
    {
        if (inputTokens != null)
        {
            this.totalInputTokens = (this.totalInputTokens != null ? this.totalInputTokens : 0) + inputTokens;
        }
        if (outputTokens != null)
        {
            this.totalOutputTokens = (this.totalOutputTokens != null ? this.totalOutputTokens : 0) + outputTokens;
        }
    }

    public static AgentRun start(String agentId, String agentName, String input)
    {
        return AgentRun.builder()
                .runId(java.util.UUID.randomUUID().toString())
                .agentId(agentId)
                .agentName(agentName)
                .input(input)
                .status(RunStatus.RUNNING)
                .startTime(Instant.now())
                .steps(new ArrayList<>())
                .build();
    }
}
