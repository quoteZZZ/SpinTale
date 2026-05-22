package com.spintale.ai.agent.definition;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AgentDefinition
{
    private String agentId;
    private String name;
    private String description;
    private AgentType type;
    private String model;
    private String systemPrompt;
    private List<String> toolIds;
    private int maxSteps;
    private long timeoutMs;
    private double temperature;
    private Map<String, Object> memoryConfig;
    private Map<String, Object> llmConfig;
    private Map<String, Object> metadata;
    private AgentStatus status;
    private Long createBy;
    private Instant createTime;
    private Instant updateTime;

    public enum AgentType
    {
        REACT,
        PLAN_AND_EXECUTE,
        CHAT,
        WORKFLOW,
        ROUTER,
        CUSTOM
    }

    public enum AgentStatus
    {
        DRAFT,
        ACTIVE,
        DEPRECATED,
        ARCHIVED
    }

    public boolean isActive()
    {
        return status == AgentStatus.ACTIVE;
    }

    public boolean hasTools()
    {
        return toolIds != null && !toolIds.isEmpty();
    }

    public int getToolCount()
    {
        return toolIds != null ? toolIds.size() : 0;
    }

    public boolean hasTool(String toolId)
    {
        return toolIds != null && toolIds.contains(toolId);
    }

    public void addTool(String toolId)
    {
        if (this.toolIds == null)
        {
            this.toolIds = new java.util.ArrayList<>();
        }
        if (!this.toolIds.contains(toolId))
        {
            this.toolIds.add(toolId);
        }
    }

    public void removeTool(String toolId)
    {
        if (this.toolIds != null)
        {
            this.toolIds.remove(toolId);
        }
    }

    public static AgentDefinition create(String name, AgentType type, String model)
    {
        return AgentDefinition.builder()
                .agentId(java.util.UUID.randomUUID().toString())
                .name(name)
                .type(type)
                .model(model)
                .maxSteps(10)
                .timeoutMs(60000L)
                .temperature(0.7)
                .status(AgentStatus.DRAFT)
                .createTime(Instant.now())
                .updateTime(Instant.now())
                .build();
    }

    public static AgentDefinition reactAgent(String name, String model, String systemPrompt)
    {
        AgentDefinition agent = create(name, AgentType.REACT, model);
        agent.setSystemPrompt(systemPrompt);
        return agent;
    }

    public static AgentDefinition chatAgent(String name, String model, String systemPrompt)
    {
        AgentDefinition agent = create(name, AgentType.CHAT, model);
        agent.setSystemPrompt(systemPrompt);
        agent.setMaxSteps(1);
        return agent;
    }
}
