package com.spintale.ai.agent.approval;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface HumanApprovalService
{
    ApprovalRequest requestApproval(String runId, String stepId,
            String toolName, String description, 
            java.util.Map<String, Object> args, 
            String riskLevel);

    CompletableFuture<ApprovalDecision> waitForDecision(String requestId, Duration timeout);

    Optional<ApprovalDecision> getDecision(String requestId);

    void approve(String requestId, String approverId, String reason);

    void reject(String requestId, String approverId, String reason);

    List<ApprovalRequest> getPendingApprovals(String approverId);

    List<ApprovalRequest> getPendingApprovalsByRisk(String riskLevel);

    void cancel(String requestId);

    record ApprovalDecision(
            String requestId,
            boolean approved,
            String approverId,
            String reason,
            Instant decidedAt
    ) {}
}
