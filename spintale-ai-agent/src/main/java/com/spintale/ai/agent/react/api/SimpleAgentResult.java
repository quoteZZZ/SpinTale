package com.spintale.ai.agent.react.api;

import java.util.List;
import java.util.Map;

public class SimpleAgentResult implements AgentResult {

    private final boolean success;
    private final String content;
    private final Map<String, Object> steps;
    private final List<String> usedTools;
    private final Object tokenUsage;

    public SimpleAgentResult(boolean success,
                             String content,
                             Map<String, Object> steps,
                             List<String> usedTools,
                             Object tokenUsage) {
        this.success = success;
        this.content = content;
        this.steps = steps;
        this.usedTools = usedTools;
        this.tokenUsage = tokenUsage;
    }

    @Override
    public boolean isSuccess() {
        return success;
    }

    @Override
    public String getContent() {
        return content;
    }

    @Override
    public Map<String, Object> getSteps() {
        return steps;
    }

    @Override
    public List<String> getUsedTools() {
        return usedTools;
    }

    @Override
    public Object getTokenUsage() {
        return tokenUsage;
    }
}
