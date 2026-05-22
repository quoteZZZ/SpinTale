package com.spintale.ai.console.vo;

import java.io.Serializable;
import lombok.Data;

@Data
public class ToolDefinitionVO implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String toolId;
    private String toolName;
    private String description;
    private String riskLevel;
    private Boolean requiresApproval;
}
