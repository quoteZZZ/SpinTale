package com.spintale.ai.agent.coordination;

import java.util.List;
import java.util.Optional;

public interface TeamCoordinator
{
    AgentTeam createTeam(String name, AgentTeam.CoordinationStrategy strategy);

    AgentTeam addAgentToTeam(String teamId, String agentId, String role);

    AgentTeam removeAgentFromTeam(String teamId, String agentId);

    Optional<AgentTeam> getTeam(String teamId);

    List<AgentTeam> listTeams();

    TeamExecution execute(String teamId, String input);

    TeamExecution executeWithContext(String teamId, String input, 
            java.util.Map<String, Object> context);

    Optional<TeamExecution> getExecution(String executionId);

    List<TeamExecution> getTeamExecutions(String teamId);

    void setTeamConfig(String teamId, String key, Object value);
}
