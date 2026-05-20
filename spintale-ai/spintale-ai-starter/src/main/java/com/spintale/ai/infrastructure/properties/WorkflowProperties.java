package com.spintale.ai.infrastructure.properties;

import lombok.Data;

/**
 * Workflow engine settings for agent execution.
 */
@Data
public class WorkflowProperties {
    private Boolean enabled = false;
    private String targetServer = "localhost:7233";
    private String taskQueue = "ai-agent-queue";
}
