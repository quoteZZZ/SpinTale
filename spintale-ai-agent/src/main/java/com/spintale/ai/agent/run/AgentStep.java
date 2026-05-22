package com.spintale.ai.agent.run;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AgentStep
{
    private String stepId;
    private String runId;
    private Integer stepNumber;
    private StepType type;
    private String name;
    private String description;
    private Instant startTime;
    private Instant endTime;
    private Long durationMs;
    private StepStatus status;
    private String input;
    private String output;
    private String thought;
    private String action;
    private String actionInput;
    private String actionOutput;
    private String toolCallId;
    private String toolName;
    private Map<String, Object> toolArgs;
    private String toolResult;
    private Long inputTokens;
    private Long outputTokens;
    private Double cost;
    private String errorMessage;
    private Map<String, Object> metadata;

    public enum StepType
    {
        THOUGHT,
        ACTION,
        TOOL_CALL,
        TOOL_RESULT,
        OBSERVATION,
        DECISION,
        REFLECTION,
        DELEGATION,
        HUMAN_INPUT,
        WAIT_FOR_APPROVAL
    }

    public enum StepStatus
    {
        PENDING,
        RUNNING,
        SUCCEEDED,
        FAILED,
        SKIPPED,
        AWAITING_APPROVAL,
        APPROVED,
        REJECTED
    }

    public void markSucceeded(String output)
    {
        this.status = StepStatus.SUCCEEDED;
        this.output = output;
        this.endTime = Instant.now();
        if (startTime != null)
        {
            this.durationMs = endTime.toEpochMilli() - startTime.toEpochMilli();
        }
    }

    public void markFailed(String error)
    {
        this.status = StepStatus.FAILED;
        this.errorMessage = error;
        this.endTime = Instant.now();
        if (startTime != null)
        {
            this.durationMs = endTime.toEpochMilli() - startTime.toEpochMilli();
        }
    }

    public void markAwaitingApproval()
    {
        this.status = StepStatus.AWAITING_APPROVAL;
        this.endTime = Instant.now();
    }

    public void markApproved()
    {
        this.status = StepStatus.APPROVED;
    }

    public void markRejected(String reason)
    {
        this.status = StepStatus.REJECTED;
        this.errorMessage = reason;
    }

    public boolean isFinished()
    {
        return status == StepStatus.SUCCEEDED 
            || status == StepStatus.FAILED 
            || status == StepStatus.SKIPPED
            || status == StepStatus.REJECTED;
    }

    public boolean needsApproval()
    {
        return status == StepStatus.AWAITING_APPROVAL;
    }

    public boolean isToolCall()
    {
        return type == StepType.TOOL_CALL || toolName != null;
    }

    public static AgentStep thought(String runId, int stepNum, String thought)
    {
        return AgentStep.builder()
                .stepId(java.util.UUID.randomUUID().toString())
                .runId(runId)
                .stepNumber(stepNum)
                .type(StepType.THOUGHT)
                .name("Thought")
                .thought(thought)
                .status(StepStatus.SUCCEEDED)
                .startTime(Instant.now())
                .build();
    }

    public static AgentStep toolCall(String runId, int stepNum, 
            String toolName, Map<String, Object> args)
    {
        return AgentStep.builder()
                .stepId(java.util.UUID.randomUUID().toString())
                .runId(runId)
                .stepNumber(stepNum)
                .type(StepType.TOOL_CALL)
                .name("Tool: " + toolName)
                .toolName(toolName)
                .toolArgs(args)
                .status(StepStatus.RUNNING)
                .startTime(Instant.now())
                .build();
    }

    public static AgentStep observation(String runId, int stepNum, String result)
    {
        return AgentStep.builder()
                .stepId(java.util.UUID.randomUUID().toString())
                .runId(runId)
                .stepNumber(stepNum)
                .type(StepType.OBSERVATION)
                .name("Observation")
                .toolResult(result)
                .status(StepStatus.SUCCEEDED)
                .startTime(Instant.now())
                .build();
    }
}
