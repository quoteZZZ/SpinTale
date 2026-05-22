package com.spintale.ai.agent.checkpoint;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

public interface CheckpointService
{
    AgentCheckpoint saveCheckpoint(AgentCheckpoint checkpoint);

    Optional<AgentCheckpoint> getCheckpoint(String checkpointId);

    Optional<AgentCheckpoint> getLatestCheckpoint(String runId);

    List<AgentCheckpoint> getCheckpoints(String runId);

    List<AgentCheckpoint> getActiveCheckpoints(String agentId);

    void resumeFromCheckpoint(String checkpointId);

    void abandonCheckpoint(String checkpointId);

    void cleanupExpiredCheckpoints();

    void setExpiration(Duration expiration);

    Optional<AgentCheckpoint> findResumableCheckpoint(String agentId, String userId);

    ResumeContext prepareResume(AgentCheckpoint checkpoint);

    record ResumeContext(
            String runId,
            String agentId,
            int fromStep,
            String stateJson,
            String nextAction,
            boolean requiresApproval
    ) {}
}
