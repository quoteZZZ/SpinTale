package com.spintale.ai.agent.run;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface AgentRunService
{
    AgentRun createRun(String agentId, String input);

    void updateRun(AgentRun run);

    void addStep(String runId, AgentStep step);

    void updateStep(AgentStep step);

    Optional<AgentRun> getRun(String runId);

    List<AgentStep> getSteps(String runId);

    Optional<AgentStep> getCurrentStep(String runId);

    List<AgentRun> queryRuns(String agentId, String userId, Instant start, Instant end, int limit);

    AgentRunSummary getSummary(String agentId);

    record AgentRunSummary(
            String agentId,
            long totalRuns,
            long successfulRuns,
            long failedRuns,
            double successRate,
            long totalTokens,
            double totalCost,
            double avgDurationMs
    ) {}
}
