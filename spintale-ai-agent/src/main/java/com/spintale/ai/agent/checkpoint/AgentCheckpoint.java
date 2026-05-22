package com.spintale.ai.agent.checkpoint;

import java.time.Instant;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AgentCheckpoint
{
    private String checkpointId;
    private String runId;
    private String agentId;
    private int stepNumber;
    private CheckpointType type;
    private CheckpointStatus status;
    private String stateJson;
    private Map<String, Object> state;
    private String currentThought;
    private String nextAction;
    private Map<String, Object> actionArgs;
    private Instant createdAt;
    private Instant expiresAt;
    private String userId;
    private String reason;

    public enum CheckpointType
    {
        BEFORE_STEP,
        AFTER_STEP,
        BEFORE_TOOL_CALL,
        AFTER_TOOL_CALL,
        BEFORE_APPROVAL,
        AFTER_APPROVAL,
        ON_ERROR,
        MANUAL
    }

    public enum CheckpointStatus
    {
        ACTIVE,
        RESUMED,
        EXPIRED,
        ABANDONED
    }

    public boolean isActive()
    {
        return status == CheckpointStatus.ACTIVE 
            && (expiresAt == null || Instant.now().isBefore(expiresAt));
    }

    public boolean canResume()
    {
        return isActive();
    }

    public boolean isExpired()
    {
        return expiresAt != null && Instant.now().isAfter(expiresAt);
    }

    public void markAsResumed()
    {
        this.status = CheckpointStatus.RESUMED;
    }

    public void markAsExpired()
    {
        this.status = CheckpointStatus.EXPIRED;
    }

    public void markAsAbandoned()
    {
        this.status = CheckpointStatus.ABANDONED;
    }

    public static AgentCheckpoint create(String runId, String agentId, 
            int stepNumber, CheckpointType type)
    {
        return AgentCheckpoint.builder()
                .checkpointId(java.util.UUID.randomUUID().toString())
                .runId(runId)
                .agentId(agentId)
                .stepNumber(stepNumber)
                .type(type)
                .status(CheckpointStatus.ACTIVE)
                .createdAt(Instant.now())
                .build();
    }

    public static AgentCheckpoint beforeToolCall(String runId, String agentId, 
            int stepNumber, String toolName, Map<String, Object> args)
    {
        AgentCheckpoint checkpoint = create(runId, agentId, stepNumber, 
                CheckpointType.BEFORE_TOOL_CALL);
        checkpoint.setNextAction(toolName);
        checkpoint.setActionArgs(args);
        return checkpoint;
    }

    public static AgentCheckpoint beforeApproval(String runId, String agentId,
            int stepNumber, String reason)
    {
        AgentCheckpoint checkpoint = create(runId, agentId, stepNumber,
                CheckpointType.BEFORE_APPROVAL);
        checkpoint.setReason(reason);
        return checkpoint;
    }

    public static AgentCheckpoint onError(String runId, String agentId,
            int stepNumber, String error)
    {
        AgentCheckpoint checkpoint = create(runId, agentId, stepNumber,
                CheckpointType.ON_ERROR);
        checkpoint.setReason(error);
        return checkpoint;
    }
}
