package com.spintale.ai.agent.coordination;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TeamExecution
{
    private String executionId;
    private String teamId;
    private String input;
    private String output;
    private ExecutionStatus status;
    private List<AgentExecution> agentExecutions;
    private Map<String, Object> context;
    private Instant startTime;
    private Instant endTime;
    private long durationMs;
    private String errorMessage;

    public enum ExecutionStatus
    {
        PENDING,
        RUNNING,
        SUCCEEDED,
        FAILED,
        PARTIAL_SUCCESS
    }

    @Data
    @Builder
    public static class AgentExecution
    {
        private String agentId;
        private String runId;
        private String role;
        private AgentExecutionStatus status;
        private String input;
        private String output;
        private Instant startTime;
        private Instant endTime;
        private long durationMs;
        private String error;
    }

    public enum AgentExecutionStatus
    {
        SKIPPED,
        RUNNING,
        SUCCEEDED,
        FAILED
    }

    public void addAgentExecution(AgentExecution execution)
    {
        if (this.agentExecutions == null)
        {
            this.agentExecutions = new java.util.ArrayList<>();
        }
        this.agentExecutions.add(execution);
    }

    public boolean isSuccess()
    {
        return status == ExecutionStatus.SUCCEEDED;
    }

    public int getSuccessCount()
    {
        if (agentExecutions == null) return 0;
        return (int) agentExecutions.stream()
                .filter(e -> e.getStatus() == AgentExecutionStatus.SUCCEEDED)
                .count();
    }

    public int getFailureCount()
    {
        if (agentExecutions == null) return 0;
        return (int) agentExecutions.stream()
                .filter(e -> e.getStatus() == AgentExecutionStatus.FAILED)
                .count();
    }
}
