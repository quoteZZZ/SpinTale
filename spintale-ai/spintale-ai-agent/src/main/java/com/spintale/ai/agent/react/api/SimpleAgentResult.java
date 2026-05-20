package com.spintale.ai.agent.react.api;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class SimpleAgentResult implements AgentResult
{
    private final boolean success;
    private final String content;
    private final Map<String, Object> steps;
    private final List<String> usedTools;
    private final Object tokenUsage;
}
