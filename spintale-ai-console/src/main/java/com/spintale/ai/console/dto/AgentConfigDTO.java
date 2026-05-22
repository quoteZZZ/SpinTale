package com.spintale.ai.console.dto;

import java.io.Serializable;
import lombok.Data;

@Data
public class AgentConfigDTO implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Long agentId;
    private String agentName;
    private String agentType;
    private String systemPrompt;
    private String model;
    private Integer status;
}
