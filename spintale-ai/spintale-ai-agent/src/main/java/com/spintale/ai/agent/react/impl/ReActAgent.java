package com.spintale.ai.agent.react.impl;

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
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;

import com.spintale.ai.agent.react.api.AgentCallback;
import com.spintale.ai.agent.react.api.AgentResult;
import com.spintale.ai.agent.react.api.AgentService;
import com.spintale.ai.agent.react.api.SimpleAgentResult;

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
public class ReActAgent implements AgentService, DisposableBean {

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
     * 改进：支持真正的 Token 级别流式输出
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
        long startTime = System.currentTimeMillis();
        List<dev.langchain4j.data.message.ChatMessage> messages = new ArrayList<>();
        String toolsDescription = buildToolsDescription();
        messages.add(new SystemMessage(String.format(REACT_SYSTEM_PROMPT, toolsDescription)));
        messages.add(new UserMessage(task));

        Map<String, Object> steps = new LinkedHashMap<>();
        List<String> executedTools = new ArrayList<>();
        int iteration = 0;
        final dev.langchain4j.model.output.TokenUsage[] totalTokenUsageRef = {null};
        Map<String, Integer> toolCallCount = new HashMap<>();

        try {
            while (iteration < defaultMaxIterations) {
                iteration++;
                log.debug("ReAct streaming iteration {}/{}", iteration, defaultMaxIterations);

                // 使用流式模型获取响应
                StringBuilder responseBuilder = new StringBuilder();
                java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
                final boolean[] hasToolCall = {false};
                final dev.langchain4j.data.message.AiMessage[] aiMessageRef = {null};
                final Map<String, Object> finalSteps = steps;
                final List<String> finalExecutedTools = executedTools;
                final int finalIteration = iteration;
                final Map<String, Integer> finalToolCallCount = toolCallCount;

                streamingChatModel.chat(messages, new dev.langchain4j.model.chat.response.StreamingChatResponseHandler() {
                    @Override
                    public void onPartialResponse(String token) {
                        responseBuilder.append(token);
                        callback.onThought(token);
                    }

                    @Override
                    public void onCompleteResponse(dev.langchain4j.model.chat.response.ChatResponse response) {
                        AiMessage aiMessage = response.aiMessage();
                        aiMessageRef[0] = aiMessage;
                        
                        // 累计 Token 用量
                        if (response.tokenUsage() != null) {
                            if (totalTokenUsageRef[0] == null) {
                                totalTokenUsageRef[0] = response.tokenUsage();
                            } else {
                                totalTokenUsageRef[0] = new dev.langchain4j.model.output.TokenUsage(
                                        totalTokenUsageRef[0].inputTokenCount() + response.tokenUsage().inputTokenCount(),
                                        totalTokenUsageRef[0].outputTokenCount() + response.tokenUsage().outputTokenCount(),
                                        totalTokenUsageRef[0].totalTokenCount() + response.tokenUsage().totalTokenCount()
                                );
                            }
                        }

                        // 检查是否有工具调用
                        if (aiMessage.hasToolExecutionRequests()) {
                            hasToolCall[0] = true;
                            List<ToolExecutionRequest> toolRequests = aiMessage.toolExecutionRequests();

                            for (ToolExecutionRequest request : toolRequests) {
                                String toolName = request.name();
                                String argsJson = request.arguments();

                                // 循环检测
                                finalToolCallCount.merge(toolName, 1, Integer::sum);
                                if (finalToolCallCount.get(toolName) > loopDetectionThreshold) {
                                    log.warn("Loop detected: tool '{}' called {} times", toolName, finalToolCallCount.get(toolName));
                                    callback.onError("检测到循环调用工具 '" + toolName + "'");
                                    latch.countDown();
                                    return;
                                }

                                log.info("Executing tool: {} with args: {}", toolName, argsJson);
                                String result = executeToolWithTimeout(toolName, argsJson);

                                Map<String, String> stepInfo = new LinkedHashMap<>();
                                stepInfo.put("tool", toolName);
                                stepInfo.put("arguments", argsJson);
                                stepInfo.put("result", truncate(result, 500));
                                finalSteps.put("step_" + finalIteration, stepInfo);
                                finalExecutedTools.add(toolName);

                                callback.onToolResult(toolName, result);

                                messages.add(aiMessage);
                                messages.add(new ToolExecutionResultMessage(request.id(), toolName, result));
                            }
                        } else {
                            // 没有工具调用，返回最终答案
                            messages.add(aiMessage);
                            String finalAnswer = aiMessage.text();
                            callback.onFinalResponse(finalAnswer);
                        }

                        latch.countDown();
                    }

                    @Override
                    public void onError(Throwable error) {
                        log.error("Streaming chat failed", error);
                        callback.onError(error.getMessage());
                        latch.countDown();
                    }
                });

                // 等待当前轮次完成
                latch.await();

                // 如果没有工具调用，退出循环
                if (!hasToolCall[0]) {
                    break;
                }
            }

            if (iteration >= defaultMaxIterations) {
                log.warn("ReAct reached max iterations ({})", defaultMaxIterations);
                callback.onError("已达到最大尝试次数 (" + defaultMaxIterations + ")");
            }

        } catch (Exception e) {
            log.error("ReAct streaming execution failed", e);
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
     * 执行工具的线程池（有界线程池，防止OOM）
     * 配置：核心线程数10，最大线程数50，队列容量1000，使用CallerRunsPolicy拒绝策略
     */
    private static final ThreadPoolExecutor TOOL_EXECUTOR = new ThreadPoolExecutor(
            10,  // 核心线程数
            50,  // 最大线程数
            60L, TimeUnit.SECONDS,  // 空闲线程存活时间
            new LinkedBlockingQueue<>(1000),  // 有界队列，容量1000
            r -> {
                Thread thread = new Thread(r, "tool-executor");
                thread.setDaemon(true);
                return thread;
            },
            new ThreadPoolExecutor.CallerRunsPolicy()  // 拒绝策略：由调用线程执行
    );

    private String executeToolWithTimeout(String toolName, String argsJson) {
        Function<Map<String, Object>, String> tool = tools.get(toolName);
        if (tool == null) {
            return "Error: tool not found - " + toolName;
        }

        try {
            Map<String, Object> args = parseJsonArgs(argsJson);
            Future<String> future = TOOL_EXECUTOR.submit(() -> {
                String result = tool.apply(args);
                return result != null ? result : "(tool returned no value)";
            });
            return future.get(toolExecutionTimeoutMs, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
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

    // ==================== 生命周期管理 ====================

    @Override
    public void destroy() throws Exception {
        log.info("Shutting down ReActAgent tool executor");
        TOOL_EXECUTOR.shutdown();
        if (!TOOL_EXECUTOR.awaitTermination(5, TimeUnit.SECONDS)) {
            TOOL_EXECUTOR.shutdownNow();
            log.warn("Tool executor forced shutdown");
        }
    }

    @PreDestroy
    public void cleanup() {
        try {
            destroy();
        } catch (Exception e) {
            log.error("Error during ReActAgent cleanup", e);
        }
    }
}
