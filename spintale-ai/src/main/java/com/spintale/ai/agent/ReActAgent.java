package com.spintale.ai.agent;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.model.chat.ChatModel;

public class ReActAgent implements AgentService
{
    private final ChatModel chatModel;
    private final Map<String, Function<Map<String, Object>, String>> tools;
    private final List<ToolSpecification> toolSpecifications;
    private final int defaultMaxIterations;

    public ReActAgent(ChatModel chatModel, Map<String, Function<Map<String, Object>, String>> tools,
            List<ToolSpecification> toolSpecifications)
    {
        this(chatModel, tools, toolSpecifications, 3);
    }

    public ReActAgent(ChatModel chatModel, Map<String, Function<Map<String, Object>, String>> tools,
            List<ToolSpecification> toolSpecifications, int defaultMaxIterations)
    {
        this.chatModel = chatModel;
        this.tools = tools;
        this.toolSpecifications = toolSpecifications;
        this.defaultMaxIterations = defaultMaxIterations;
    }

    @Override
    public AgentResult execute(String task)
    {
        return execute(task, defaultMaxIterations);
    }

    @Override
    public AgentResult execute(String task, int maxIterations)
    {
        Map<String, Object> steps = new LinkedHashMap<>();
        steps.put("mode", toolSpecifications == null || toolSpecifications.isEmpty() ? "plain-chat" : "tool-ready");
        String content = chatModel.chat(task);
        return new SimpleAgentResult(true, content, steps, new ArrayList<>(), null);
    }

    @Override
    public AgentResult executeWithTools(String task, List<String> toolNames)
    {
        Map<String, Object> steps = new LinkedHashMap<>();
        steps.put("requestedTools", toolNames);
        steps.put("registeredToolCount", tools == null ? 0 : tools.size());
        String content = chatModel.chat(task);
        return new SimpleAgentResult(true, content, steps, toolNames == null ? List.of() : toolNames, null);
    }

    @Override
    public void executeStreaming(String task, AgentCallback callback)
    {
        try
        {
            callback.onFinalResponse(execute(task).getContent());
        }
        catch (Exception e)
        {
            callback.onError(e.getMessage());
        }
    }
}
