package com.spintale.ai.agent;

import dev.langchain4j.model.output.TokenUsage;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 简单的 Agent 结果实现
 */
@Data
@AllArgsConstructor
public class SimpleAgentResult implements AgentResult {
    
    private final boolean success;
    private final String content;
    private final Map<String, Object> steps;
    private final List<String> usedTools;
    private final TokenUsage tokenUsage;
    
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
    public TokenUsage getTokenUsage() {
        return tokenUsage;
    }
}
