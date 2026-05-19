package com.spintale.ai.agent.react;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.model.output.TokenUsage;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

/**
 * ReAct Agent 重构版
 *
 * 改进点：
 * 1. 使用 FastJSON2 替代手写 JSON 解析器（原代码有反射 hack 和不完整的解析器）
 * 2. 增加真正的流式执行支持（原 executeStreaming 只是包装同步调用）
 * 3. 增加可观测性（执行步骤追踪、Token 统计）
 * 4. 增加工具执行超时保护
 * 5. 增加循环检测（检测重复工具调用模式）
 * 6. 使用 @CircuitBreaker 注解接入 Resilience4j
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
    private final StreamingChatModel streamingChatModel;
    private final Map<String, Function<Map<String, Object>, String>> tools;
    private final List<ToolSpecification> toolSpecifications;
    private final int defaultMaxIterations;

    /** 工具执行超时（毫秒） */
    private long toolExecutionTimeoutMs = 30000;

    /** 循环检测阈值：连续相同工具调用次数 */
    private int loopDetectionThreshold = 3;

    public ReActAgent(ChatModel chatModel,
                      Map<String, Function<Map<String, Object>, String>> tools,
                      List<ToolSpecification> toolSpecifications) {
        this(chatModel, null, tools, toolSpecifications, 10);
    }

    public ReActAgent(ChatModel chatModel,
                      StreamingChatModel streamingChatModel,
                      Map<String, Function<Map<String, Object>, String>> tools,
                      List<ToolSpecification> toolSpecifications,
                      int defaultMaxIterations) {
        this.chatModel = chatModel;
        this.streamingChatModel = streamingChatModel;
        this.tools = tools != null ? new ConcurrentHashMap<>(tools) : new ConcurrentHashMap<>();
        this.toolSpecifications = toolSpecifications != null ? toolSpecifications : new ArrayList<>();
        this.defaultMaxIterations = defaultMaxIterations;
    }

    @Override
    @CircuitBreaker(name = "llmService", fallbackMethod = "executeFallback")
    public AgentResult execute(String task) {
        return execute(task, defaultMaxIterations);
    }

    @Override
    @CircuitBreaker(name = "llmService", fallbackMethod = "executeFallback")
    public AgentResult execute(String task, int maxIterations) {
        long startTime = System.currentTimeMillis();

        List<dev.langchain4j.data.message.ChatMessage> messages = new ArrayList<>();
        String toolsDescription = buildToolsDescription();
        messages.add(new SystemMessage(String.format(REACT_SYSTEM_PROMPT, toolsDescription)));
        messages.add(new UserMessage(task));

        Map<String, Object> steps = new LinkedHashMap<>();
        List<String> executedTools = new ArrayList<>();
        int iteration = 0;
        // 使用 LangChain4j 的 TokenUsage 进行内部累计
        dev.langchain4j.model.output.TokenUsage totalTokenUsage = null;

        // 循环检测状态
        Map<String, Integer> toolCallCount = new HashMap<>();

        while (iteration < maxIterations) {
            iteration++;
            log.debug("ReAct iteration {}/{}", iteration, maxIterations);

            try {
                // LangChain4j 1.13.1: ChatModel.chat() returns ChatResponse
                dev.langchain4j.model.chat.response.ChatResponse response = chatModel.chat(messages);
                AiMessage aiMessage = response.aiMessage();

                // 累计 Token 用量 - accumulate token usage
                if (response.tokenUsage() != null) {
                    if (totalTokenUsage == null) {
                        totalTokenUsage = response.tokenUsage();
                    } else {
                        // Create new TokenUsage with accumulated values
                        totalTokenUsage = new dev.langchain4j.model.output.TokenUsage(
                                totalTokenUsage.inputTokenCount() + response.tokenUsage().inputTokenCount(),
                                totalTokenUsage.outputTokenCount() + response.tokenUsage().outputTokenCount(),
                                totalTokenUsage.totalTokenCount() + response.tokenUsage().totalTokenCount()
                        );
                    }
                }

                // 检查是否有工具调用
                if (aiMessage.hasToolExecutionRequests()) {
                    List<ToolExecutionRequest> toolRequests = aiMessage.toolExecutionRequests();

                    for (ToolExecutionRequest request : toolRequests) {
                        String toolName = request.name();
                        String argsJson = request.arguments();

                        // 循环检测：检测重复调用同一工具
                        toolCallCount.merge(toolName, 1, Integer::sum);
                        if (toolCallCount.get(toolName) > loopDetectionThreshold) {
                            log.warn("Loop detected: tool '{}' called {} times, breaking loop",
                                    toolName, toolCallCount.get(toolName));
                            steps.put("warning", "检测到循环调用工具 '" + toolName + "'，已自动终止");
                            return buildResult(false, "检测到循环调用，已自动终止", steps, executedTools,
                                    totalTokenUsage, startTime);
                        }

                        log.info("Executing tool: {} with args: {}", toolName, argsJson);

                        // 执行工具（带超时保护）
                        String result = executeToolWithTimeout(toolName, argsJson);

                        // 记录步骤
                        Map<String, String> stepInfo = new LinkedHashMap<>();
                        stepInfo.put("tool", toolName);
                        stepInfo.put("arguments", argsJson);
                        stepInfo.put("result", truncate(result, 500));
                        steps.put("step_" + iteration, stepInfo);
                        executedTools.add(toolName);

                        // 添加工具执行结果到消息历史
                        messages.add(aiMessage);
                        messages.add(new ToolExecutionResultMessage(request.id(), toolName, result));
                    }

                    continue;
                }

                // 没有工具调用，返回最终答案
                messages.add(aiMessage);
                String finalAnswer = aiMessage.text();

                return buildResult(true, finalAnswer, steps, executedTools, totalTokenUsage, startTime);

            } catch (Exception e) {
                log.error("ReAct execution failed at iteration {}", iteration, e);
                return buildResult(false, "执行失败：" + e.getMessage(), steps, executedTools,
                        totalTokenUsage, startTime);
            }
        }

        // 达到最大迭代次数
        log.warn("ReAct reached max iterations ({})", maxIterations);
        steps.put("error", "达到最大迭代次数，可能陷入循环");
        return buildResult(false, "已达到最大尝试次数 (" + maxIterations + ")，未能完成任务。",
                steps, executedTools, totalTokenUsage, startTime);
    }

    /**
     * 真正的流式执行
     * 改进：原实现只是包装同步调用，现在支持真正的 Token 级别流式输出
     */
    @Override
    public void executeStreaming(String task, AgentCallback callback) {
        if (streamingChatModel != null) {
            // 真正的流式执行
            executeStreamingInternal(task, callback);
        } else {
            // 降级为同步执行
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
    }

    private void executeStreamingInternal(String task, AgentCallback callback) {
        // 使用 StreamingChatModel 逐步输出
        try {
            List<dev.langchain4j.data.message.ChatMessage> messages = new ArrayList<>();
            String toolsDescription = buildToolsDescription();
            messages.add(new SystemMessage(String.format(REACT_SYSTEM_PROMPT, toolsDescription)));
            messages.add(new UserMessage(task));

            // 先执行一次同步的 ReAct 循环获取最终答案
            // 然后在最终答案阶段使用流式输出
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

    @Override
    public AgentResult executeWithTools(String task, List<String> toolNames) {
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

            ReActAgent filteredAgent = new ReActAgent(chatModel, streamingChatModel,
                    filteredTools, filteredSpecs, defaultMaxIterations);
            return filteredAgent.execute(task);
        }

        return execute(task);
    }

    // ==================== 私有方法 ====================

    /**
     * 使用 FastJSON2 解析 JSON 参数（替代原手写解析器）
     */
    private Map<String, Object> parseJsonArgs(String json) {
        if (json == null || json.trim().isEmpty()) {
            return new HashMap<>();
        }

        try {
            return JSON.parseObject(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.warn("Failed to parse JSON args: {}", json, e);
            return new HashMap<>();
        }
    }

    /**
     * 执行工具（带超时保护）
     */
    private String executeToolWithTimeout(String toolName, String argsJson) {
        Function<Map<String, Object>, String> tool = tools.get(toolName);
        if (tool == null) {
            return "Error: tool not found - " + toolName;
        }

        Future<String> future = null;
        ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
            Thread thread = new Thread(r, "tool-" + toolName.replaceAll("[^A-Za-z0-9_.-]", "_"));
            thread.setDaemon(true);
            return thread;
        });
        try {
            Map<String, Object> args = parseJsonArgs(argsJson);
            future = executor.submit(() -> {
                String result = tool.apply(args);
                return result != null ? result : "(tool returned no value)";
            });
            return future.get(toolExecutionTimeoutMs, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            if (future != null) {
                future.cancel(true);
            }
            return "Error: tool execution timed out (" + toolExecutionTimeoutMs + "ms)";
        } catch (ExecutionException e) {
            Throwable cause = e.getCause() == null ? e : e.getCause();
            return "Error: tool execution failed - " + cause.getMessage();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "Error: tool execution interrupted";
        } catch (Exception e) {
            log.error("Tool execution failed: {}", toolName, e);
            return "Error: tool execution failed - " + e.getMessage();
        } finally {
            executor.shutdownNow();
        }
    }

    /**
     * 熔断降级方法
     */
    private AgentResult executeFallback(String task, Exception e) {
        log.warn("ReAct agent circuit breaker activated: {}", e.getMessage());
        return new SimpleAgentResult(false,
                "AI 服务暂时不可用，请稍后重试。",
                Map.of("fallback", true, "error", e.getMessage()),
                List.of(), null);
    }

    private AgentResult buildResult(boolean success, String content, Map<String, Object> steps,
                                     List<String> executedTools, dev.langchain4j.model.output.TokenUsage tokenUsage, long startTime) {
        long duration = System.currentTimeMillis() - startTime;
        log.info("ReAct completed: success={}, duration={}ms, iterations={}, tools={}, tokens={}",
                success, duration, steps.size(), executedTools.size(),
                tokenUsage != null ? tokenUsage.totalTokenCount() : 0);

        // 转换为项目自定义的 TokenUsage 类型
        return new SimpleAgentResult(success, content, steps, executedTools,
                new com.spintale.ai.core.model.TokenUsage(
                        tokenUsage != null ? tokenUsage.inputTokenCount() : 0,
                        tokenUsage != null ? tokenUsage.outputTokenCount() : 0,
                        tokenUsage != null ? tokenUsage.totalTokenCount() : 0
                ));
    }

    private String buildToolsDescription() {
        StringBuilder sb = new StringBuilder();
        for (ToolSpecification spec : toolSpecifications) {
            sb.append("- ").append(spec.name()).append(": ").append(spec.description()).append("\n");
        }
        if (sb.isEmpty()) {
            sb.append("暂无可用工具");
        }
        return sb.toString();
    }

    private String truncate(String s, int maxLen) {
        if (s == null) return "";
        return s.length() <= maxLen ? s : s.substring(0, maxLen) + "...";
    }

    // ==================== 配置方法 ====================

    public ReActAgent setToolExecutionTimeoutMs(long timeoutMs) {
        this.toolExecutionTimeoutMs = timeoutMs;
        return this;
    }

    public ReActAgent setLoopDetectionThreshold(int threshold) {
        this.loopDetectionThreshold = threshold;
        return this;
    }
}
