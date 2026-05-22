package com.spintale.ai.agent.approval;

import java.time.Instant;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApprovalRequest
{
    private String requestId;
    private String runId;
    private String stepId;
    private String toolName;
    private Map<String, Object> toolArgs;
    private String toolDescription;
    private String riskLevel;
    private String requesterId;
    private String requesterName;
    private Instant createdAt;
    private ApprovalStatus status;
    private String approvalReason;

    public enum ApprovalStatus
    {
        PENDING,
        APPROVED,
        REJECTED,
        TIMEOUT,
        CANCELLED
    }

    public boolean isPending()
    {
        return status == ApprovalStatus.PENDING;
    }

    public boolean isDecided()
    {
        return status != ApprovalStatus.PENDING;
    }

    public void approve(String reason)
    {
        this.status = ApprovalStatus.APPROVED;
        this.approvalReason = reason;
    }

    public void reject(String reason)
    {
        this.status = ApprovalStatus.REJECTED;
        this.approvalReason = reason;
    }

    public void timeout()
    {
        this.status = ApprovalStatus.TIMEOUT;
    }

    public static ApprovalRequest create(String runId, String stepId,
            String toolName, Map<String, Object> args, String riskLevel)
    {
        return ApprovalRequest.builder()
                .requestId(java.util.UUID.randomUUID().toString())
                .runId(runId)
                .stepId(stepId)
                .toolName(toolName)
                .toolArgs(args)
                .riskLevel(riskLevel)
                .status(ApprovalStatus.PENDING)
                .createdAt(Instant.now())
                .build();
    }
}
