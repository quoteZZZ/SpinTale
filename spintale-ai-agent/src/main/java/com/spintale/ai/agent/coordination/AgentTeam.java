package com.spintale.ai.agent.coordination;

import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AgentTeam
{
    private String teamId;
    private String name;
    private String description;
    private List<AgentRole> agents;
    private CoordinationStrategy strategy;
    private String routerAgentId;
    private Map<String, Object> config;

    public enum CoordinationStrategy
    {
        ROUTER,
        SEQUENTIAL,
        PARALLEL,
        HIERARCHICAL,
        ROUND_ROBIN
    }

    @Data
    @Builder
    public static class AgentRole
    {
        private String agentId;
        private String role;
        private String description;
        private List<String> capabilities;
        private int priority;
        private double weight;
    }

    public boolean hasAgent(String agentId)
    {
        return agents != null && agents.stream()
                .anyMatch(a -> a.getAgentId().equals(agentId));
    }

    public int getAgentCount()
    {
        return agents != null ? agents.size() : 0;
    }

    public static AgentTeam create(String name, CoordinationStrategy strategy)
    {
        return AgentTeam.builder()
                .teamId(java.util.UUID.randomUUID().toString())
                .name(name)
                .strategy(strategy)
                .build();
    }

    public static AgentTeam routerTeam(String name, String routerAgentId, 
            List<AgentRole> workers)
    {
        return AgentTeam.builder()
                .teamId(java.util.UUID.randomUUID().toString())
                .name(name)
                .strategy(CoordinationStrategy.ROUTER)
                .routerAgentId(routerAgentId)
                .agents(workers)
                .build();
    }

    public static AgentTeam sequentialTeam(String name, List<AgentRole> agents)
    {
        return AgentTeam.builder()
                .teamId(java.util.UUID.randomUUID().toString())
                .name(name)
                .strategy(CoordinationStrategy.SEQUENTIAL)
                .agents(agents)
                .build();
    }
}
