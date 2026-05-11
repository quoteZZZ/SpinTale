package com.spintale.ai.agent;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.output.Response;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * 基于 LangChain4j 的 ReAct Agent 实现
 * 
 * ReAct (Reasoning + Acting) 是一种结合推理和行动的智能体架构
 */
@Slf4j
public class ReActAgent implements AgentService {
    
    private final ChatLanguageModel chatModel;
    private final Map<String, Function<Map<String, Object>, String>> tools;
    private final List<ToolSpecification> toolSpecifications;
    private final int defaultMaxIterations;
    
    public ReActAgent(
            ChatLanguageModel chatModel,
            Map<String, Function<Map<String, Object>, String>> tools,
            List<ToolSpecification> toolSpecifications) {
        this(chatModel, tools, toolSpecifications, 10);
    }
    
    public ReActAgent(
            ChatLanguageModel chatModel,
            Map<String, Function<Map<String, Object>, String>> tools,
            List<ToolSpecification> toolSpecifications,
            int defaultMaxIterations) {
        this.chatModel = chatModel;
        this.tools = tools;
        this.toolSpecifications = toolSpecifications;
        this.defaultMaxIterations = defaultMaxIterations;
    }
    
    @Override
    public AgentResult execute(String task) {
        return execute(task, defaultMaxIterations);
    }
    
    @Override
    public AgentResult execute(String task, int maxIterations) {
        log.info("Executing task: {} with maxIterations={}", task, maxIterations);
        
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(UserMessage.from(task));
        
        Map<String, Object> steps = new ConcurrentHashMap<>();
        List<String> usedTools = new ArrayList<>();
        
        for (int iteration = 0; iteration < maxIterations; iteration++) {
            log.debug("Iteration {}", iteration);
            
            // 调用模型获取响应
            Response<AiMessage> response = chatModel.generate(messages, toolSpecifications);
            AiMessage aiMessage = response.content();
            
            // 检查是否需要调用工具
            if (aiMessage.hasToolExecutionRequests()) {
                var toolRequests = aiMessage.toolExecutionRequests();
                
                for (var request : toolRequests) {
                    String toolName = request.name();
                    String argsJson = request.arguments();
                    
                    log.debug("Tool call: {} with args: {}", toolName, argsJson);
                    
                    // 执行工具
                    Function<Map<String, Object>, String> toolFunc = tools.get(toolName);
                    if (toolFunc == null) {
                        log.warn("Tool not found: {}", toolName);
                        continue;
                    }
                    
                    // 解析参数并执行
                    Map<String, Object> args = parseArguments(argsJson);
                    String result = toolFunc.apply(args);
                    
                    usedTools.add(toolName);
                    steps.put("step_" + iteration + "_tool_" + toolName, result);
                    
                    // 添加工具执行结果到消息历史
                    messages.add(ToolExecutionResultMessage.from(request.id(), toolName, result));
                }
            } else {
                // 没有工具调用，返回最终回复
                String content = aiMessage.text();
                log.info("Task completed in {} iterations", iteration + 1);
                
                return new SimpleAgentResult(true, content, steps, usedTools, response.tokenUsage());
            }
        }
        
        log.warn("Task reached max iterations ({})", maxIterations);
        return new SimpleAgentResult(false, "Reached maximum iterations without completing the task", 
                steps, usedTools, null);
    }
    
    @Override
    public AgentResult executeWithTools(String task, List<String> toolNames) {
        // TODO: 实现自定义工具集的执行
        return execute(task);
    }
    
    @Override
    public void executeStreaming(String task, AgentCallback callback) {
        // TODO: 实现流式执行
        try {
            AgentResult result = execute(task);
            callback.onFinalResponse(result.getContent());
        } catch (Exception e) {
            callback.onError(e.getMessage());
        }
    }
    
    /**
     * 解析 JSON 参数
     */
    private Map<String, Object> parseArguments(String json) {
        // 简化实现，实际应该使用 JSON 库解析
        Map<String, Object> args = new HashMap<>();
        // TODO: 使用 fastjson2 或其他 JSON 库解析
        return args;
    }
}
