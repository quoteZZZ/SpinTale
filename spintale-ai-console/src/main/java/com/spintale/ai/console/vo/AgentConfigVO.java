package com.spintale.ai.console.vo;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class AgentConfigVO implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Long agentId;
    private String agentName;
    private String agentType;
    private String systemPrompt;
    private String model;
    private Integer toolCount;
    private Integer status;
    private LocalDateTime createTime;
}
