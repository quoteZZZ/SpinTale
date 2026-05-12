package com.spintale.ai.agent;

import java.util.List;
import java.util.Map;

public class SimpleAgentResult implements AgentResult
{
    private final boolean success;
    private final String content;
    private final Map<String, Object> steps;
    private final List<String> usedTools;
    private final Object tokenUsage;

    public SimpleAgentResult(boolean success, String content, Map<String, Object> steps, List<String> usedTools, Object tokenUsage)
    {
        this.success = success;
        this.content = content;
        this.steps = steps;
        this.usedTools = usedTools;
        this.tokenUsage = tokenUsage;
    }

    public boolean isSuccess() { return success; }
    public String getContent() { return content; }
    public Map<String, Object> getSteps() { return steps; }
    public List<String> getUsedTools() { return usedTools; }
    public Object getTokenUsage() { return tokenUsage; }
}
