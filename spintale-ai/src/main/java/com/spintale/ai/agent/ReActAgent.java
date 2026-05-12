package com.spintale.ai.agent;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.model.output.TokenUsage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * ReAct Agent 实现
 * 完整的 Reasoning + Acting 循环：思考 → 行动 → 观察 → 重复
 */
public class ReActAgent implements AgentService {
    
    private static final Logger log = LoggerFactory.getLogger(ReActAgent.class);
    
    private static final String REACT_SYSTEM_PROMPT = """
            你是一个智能助手，使用 ReAct（Reasoning + Acting）模式来解决问题。
            
            对于每个用户请求，请遵循以下步骤：
            1. 思考（Thought）：分析用户需求，决定是否需要使用工具
            2. 行动（Action）：如果需要，选择一个工具并执行
            3. 观察（Observation）：查看工具执行结果
            4. 重复：根据观察结果继续思考，直到问题解决
            
            可用工具：
            %s
            
            回答格式：
            Thought: <你的思考过程>
            Action: <工具名称>
            Action Input: <JSON 格式的参数>
            Observation: <工具返回结果>
            ...（重复上述步骤）...
            Thought: 我已经获得了足够的信息
            Final Answer: <最终回答>
            
            如果不需要使用工具，直接提供 Final Answer。
            """;
    
    private final ChatModel chatModel;
    private final Map<String, Function<Map<String, Object>, String>> tools;
    private final List<ToolSpecification> toolSpecifications;
    private final int defaultMaxIterations;
    
    public ReActAgent(ChatModel chatModel, 
                     Map<String, Function<Map<String, Object>, String>> tools,
                     List<ToolSpecification> toolSpecifications) {
        this(chatModel, tools, toolSpecifications, 10);
    }
    
    public ReActAgent(ChatModel chatModel,
                     Map<String, Function<Map<String, Object>, String>> tools,
                     List<ToolSpecification> toolSpecifications,
                     int defaultMaxIterations) {
        this.chatModel = chatModel;
        this.tools = tools != null ? new ConcurrentHashMap<>(tools) : new ConcurrentHashMap<>();
        this.toolSpecifications = toolSpecifications != null ? toolSpecifications : new ArrayList<>();
        this.defaultMaxIterations = defaultMaxIterations;
    }
    
    @Override
    public AgentResult execute(String task) {
        return execute(task, defaultMaxIterations);
    }
    
    @Override
    public AgentResult execute(String task, int maxIterations) {
        long startTime = System.currentTimeMillis();
        
        List<ChatMessage> messages = new ArrayList<>();
        String toolsDescription = buildToolsDescription();
        messages.add(new SystemMessage(String.format(REACT_SYSTEM_PROMPT, toolsDescription)));
        messages.add(new UserMessage(task));
        
        Map<String, Object> steps = new HashMap<>();
        List<String> executedTools = new ArrayList<>();
        int iteration = 0;
        TokenUsage totalTokenUsage = new TokenUsage(0, 0, 0);
        
        while (iteration < maxIterations) {
            iteration++;
            log.debug("ReAct iteration {}/{}", iteration, maxIterations);
            
            try {
                Response<AiMessage> response = chatModel.generate(messages, toolSpecifications);
                AiMessage aiMessage = response.content();
                
                if (aiMessage.tokenUsage() != null) {
                    totalTokenUsage = TokenUsage.from(
                            totalTokenUsage.inputTokenCount() + aiMessage.tokenUsage().inputTokenCount(),
                            totalTokenUsage.outputTokenCount() + aiMessage.tokenUsage().outputTokenCount(),
                            totalTokenUsage.totalTokenCount() + aiMessage.tokenUsage().totalTokenCount()
                    );
                }
                
                // 检查是否有工具调用
                if (aiMessage.hasToolExecutionRequests()) {
                    List<ToolExecutionRequest> toolRequests = aiMessage.toolExecutionRequests();
                    
                    for (ToolExecutionRequest request : toolRequests) {
                        String toolName = request.name();
                        String argsJson = request.arguments();
                        
                        log.info("Executing tool: {} with args: {}", toolName, argsJson);
                        
                        // 执行工具
                        String result = executeTool(toolName, argsJson);
                        
                        // 记录步骤
                        Map<String, String> stepInfo = new HashMap<>();
                        stepInfo.put("tool", toolName);
                        stepInfo.put("arguments", argsJson);
                        stepInfo.put("result", result);
                        steps.put("step_" + iteration, stepInfo);
                        executedTools.add(toolName);
                        
                        // 添加工具执行结果到消息历史
                        messages.add(aiMessage);
                        messages.add(new ToolExecutionResultMessage(request.id(), toolName, result));
                    }
                    
                    // 继续下一轮迭代
                    continue;
                }
                
                // 没有工具调用，返回最终答案
                messages.add(aiMessage);
                String finalAnswer = aiMessage.text();
                
                long duration = System.currentTimeMillis() - startTime;
                log.info("ReAct completed in {}ms, iterations: {}, tokens: {}", 
                        duration, iteration, totalTokenUsage.totalTokenCount());
                
                return new SimpleAgentResult(true, finalAnswer, steps, executedTools, 
                        new com.spintale.ai.core.TokenUsage(
                                totalTokenUsage.inputTokenCount(),
                                totalTokenUsage.outputTokenCount(),
                                totalTokenUsage.totalTokenCount()
                        ));
                
            } catch (Exception e) {
                log.error("ReAct execution failed at iteration {}", iteration, e);
                return new SimpleAgentResult(false, "执行失败：" + e.getMessage(), steps, 
                        executedTools, null);
            }
        }
        
        // 达到最大迭代次数
        log.warn("ReAct reached max iterations ({})", maxIterations);
        steps.put("error", "达到最大迭代次数，可能陷入循环");
        return new SimpleAgentResult(false, "已达到最大尝试次数 (" + maxIterations + ")，未能完成任务。", 
                steps, executedTools, null);
    }
    
    @Override
    public AgentResult executeWithTools(String task, List<String> toolNames) {
        // 过滤只使用指定的工具
        if (toolNames != null && !toolNames.isEmpty()) {
            Map<String, Function<Map<String, Object>, String>> filteredTools = new HashMap<>();
            List<ToolSpecification> filteredSpecs = new ArrayList<>();
            
            for (String name : toolNames) {
                if (tools.containsKey(name)) {
                    filteredTools.put(name, tools.get(name));
                }
                for (ToolSpecification spec : toolSpecifications) {
                    if (spec.name().equals(name)) {
                        filteredSpecs.add(spec);
                        break;
                    }
                }
            }
            
            ReActAgent filteredAgent = new ReActAgent(chatModel, filteredTools, filteredSpecs, defaultMaxIterations);
            return filteredAgent.execute(task);
        }
        
        return execute(task);
    }
    
    @Override
    public void executeStreaming(String task, AgentCallback callback) {
        try {
            AgentResult result = execute(task);
            if (result.isSuccess()) {
                callback.onFinalResponse(result.getContent());
            } else {
                callback.onError(result.getContent());
            }
        } catch (Exception e) {
            callback.onError(e.getMessage());
        }
    }
    
    /**
     * 构建工具描述文本
     */
    private String buildToolsDescription() {
        StringBuilder sb = new StringBuilder();
        for (ToolSpecification spec : toolSpecifications) {
            sb.append("- ").append(spec.name()).append(": ").append(spec.description()).append("\n");
        }
        if (sb.length() == 0) {
            sb.append("暂无可用工具");
        }
        return sb.toString();
    }
    
    /**
     * 执行工具
     */
    private String executeTool(String toolName, String argsJson) {
        Function<Map<String, Object>, String> tool = tools.get(toolName);
        if (tool == null) {
            return "错误：未找到工具 '" + toolName + "'";
        }
        
        try {
            // 解析 JSON 参数
            Map<String, Object> args = parseJson(argsJson);
            return tool.apply(args);
        } catch (Exception e) {
            log.error("Tool execution failed: {}", toolName, e);
            return "错误：执行失败 - " + e.getMessage();
        }
    }
    
    /**
     * 简单的 JSON 解析（生产环境应使用 Jackson 或 FastJSON）
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return new HashMap<>();
        }
        
        try {
            // 尝试使用 FastJSON（如果可用）
            try {
                Class<?> clazz = Class.forName("com.alibaba.fastjson2.JSON");
                java.lang.reflect.Method method = clazz.getMethod("parseObject", String.class);
                return (Map<String, Object>) method.invoke(null, json);
            } catch (Exception e) {
                // FastJSON 不可用，使用简单解析
            }
            
            // 简单 JSON 解析（仅支持扁平对象）
            Map<String, Object> result = new HashMap<>();
            json = json.trim();
            if (json.startsWith("{") && json.endsWith("}")) {
                json = json.substring(1, json.length() - 1).trim();
                if (!json.isEmpty()) {
                    String[] pairs = json.split(",");
                    for (String pair : pairs) {
                        String[] kv = pair.split(":", 2);
                        if (kv.length == 2) {
                            String key = kv[0].trim().replaceAll("\"", "");
                            String value = kv[1].trim();
                            if (value.startsWith("\"") && value.endsWith("\"")) {
                                value = value.substring(1, value.length() - 1);
                            } else if ("true".equals(value)) {
                                value = "true";
                            } else if ("false".equals(value)) {
                                value = "false";
                            } else if ("null".equals(value)) {
                                value = null;
                            }
                            result.put(key, value);
                        }
                    }
                }
            }
            return result;
        } catch (Exception e) {
            log.warn("Failed to parse JSON: {}", json, e);
            return new HashMap<>();
        }
    }
}
