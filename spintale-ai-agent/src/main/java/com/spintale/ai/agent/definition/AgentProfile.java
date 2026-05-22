package com.spintale.ai.agent.definition;

import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AgentProfile
{
    private String profileId;
    private String agentId;
    private String name;
    private String description;
    private Persona persona;
    private List<String> capabilities;
    private List<String> constraints;
    private Map<String, Object> preferences;
    private String avatar;
    private String color;
    private boolean isDefault;

    @Data
    @Builder
    public static class Persona
    {
        private String role;
        private String tone;
        private String style;
        private String expertise;
        private List<String> traits;
    }

    public boolean hasCapability(String capability)
    {
        return capabilities != null && capabilities.contains(capability);
    }

    public void addCapability(String capability)
    {
        if (this.capabilities == null)
        {
            this.capabilities = new java.util.ArrayList<>();
        }
        if (!this.capabilities.contains(capability))
        {
            this.capabilities.add(capability);
        }
    }

    public boolean hasConstraint(String constraint)
    {
        return constraints != null && constraints.contains(constraint);
    }

    public void addConstraint(String constraint)
    {
        if (this.constraints == null)
        {
            this.constraints = new java.util.ArrayList<>();
        }
        if (!this.constraints.contains(constraint))
        {
            this.constraints.add(constraint);
        }
    }

    public String buildSystemPrompt()
    {
        StringBuilder sb = new StringBuilder();

        if (persona != null)
        {
            if (persona.getRole() != null)
            {
                sb.append("你是一个").append(persona.getRole()).append("。");
            }
            if (persona.getExpertise() != null)
            {
                sb.append("你擅长").append(persona.getExpertise()).append("。");
            }
            if (persona.getTone() != null)
            {
                sb.append("你的语气应该是").append(persona.getTone()).append("。");
            }
            if (persona.getStyle() != null)
            {
                sb.append("你的回答风格是").append(persona.getStyle()).append("。");
            }
        }

        if (constraints != null && !constraints.isEmpty())
        {
            sb.append("\n\n限制条件：\n");
            for (int i = 0; i < constraints.size(); i++)
            {
                sb.append(i + 1).append(". ").append(constraints.get(i)).append("\n");
            }
        }

        return sb.toString();
    }

    public static AgentProfile defaultProfile(String agentId)
    {
        return AgentProfile.builder()
                .profileId(java.util.UUID.randomUUID().toString())
                .agentId(agentId)
                .name("默认配置")
                .persona(Persona.builder()
                        .role("AI助手")
                        .tone("专业、友好")
                        .style("简洁明了")
                        .build())
                .isDefault(true)
                .build();
    }

    public static AgentProfile expertProfile(String agentId, String expertise)
    {
        return AgentProfile.builder()
                .profileId(java.util.UUID.randomUUID().toString())
                .agentId(agentId)
                .name("专家模式")
                .persona(Persona.builder()
                        .role(expertise + "专家")
                        .tone("专业、严谨")
                        .style("详细深入")
                        .expertise(expertise)
                        .build())
                .isDefault(false)
                .build();
    }
}
