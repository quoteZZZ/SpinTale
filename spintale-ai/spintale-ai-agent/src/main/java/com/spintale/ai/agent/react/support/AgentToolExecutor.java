package com.spintale.ai.agent.react.support;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Function;

/**
 * Agent工具执行器
 * 负责工具调用、超时控制、线程池管理
 */
public class AgentToolExecutor {

    private static final Logger log = LoggerFactory.getLogger(AgentToolExecutor.class);

    private static final ThreadPoolExecutor DEFAULT_EXECUTOR = new ThreadPoolExecutor(
            10, 50, 60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(1000),
            r -> {
                Thread thread = new Thread(r, "agent-tool-executor");
                thread.setDaemon(true);
                return thread;
            },
            new ThreadPoolExecutor.CallerRunsPolicy()
    );

    private final ThreadPoolExecutor executor;
    private final long defaultTimeoutMs;

    public AgentToolExecutor() {
        this(DEFAULT_EXECUTOR, 30000);
    }

    public AgentToolExecutor(ThreadPoolExecutor executor, long defaultTimeoutMs) {
        this.executor = executor != null ? executor : DEFAULT_EXECUTOR;
        this.defaultTimeoutMs = defaultTimeoutMs;
    }

    /**
     * 执行工具（带超时保护）
     */
    public ToolExecutionResult execute(
            String toolName,
            String argsJson,
            Function<Map<String, Object>, String> tool,
            long timeoutMs) {
        
        if (tool == null) {
            return ToolExecutionResult.failure("Tool not found: " + toolName);
        }

        Map<String, Object> args = parseJsonArgs(argsJson);
        
        try {
            Future<String> future = executor.submit(() -> {
                String result = tool.apply(args);
                return result != null ? result : "(tool returned no value)";
            });
            
            String result = future.get(timeoutMs, TimeUnit.MILLISECONDS);
            return ToolExecutionResult.success(result);
            
        } catch (TimeoutException e) {
            log.error("Tool execution timeout: tool={}, timeout={}ms", toolName, timeoutMs);
            return ToolExecutionResult.failure("Tool execution timed out after " + timeoutMs + "ms");
        } catch (ExecutionException e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            log.error("Tool execution failed: tool={}", toolName, cause);
            return ToolExecutionResult.failure("Tool execution failed: " + cause.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ToolExecutionResult.failure("Tool execution interrupted");
        } catch (Exception e) {
            log.error("Unexpected error executing tool: {}", toolName, e);
            return ToolExecutionResult.failure("Unexpected error: " + e.getMessage());
        }
    }

    /**
     * 使用默认超时执行工具
     */
    public ToolExecutionResult execute(
            String toolName,
            String argsJson,
            Function<Map<String, Object>, String> tool) {
        return execute(toolName, argsJson, tool, defaultTimeoutMs);
    }

    /**
     * 解析JSON参数
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
     * 关闭线程池
     */
    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 工具执行结果
     */
    public record ToolExecutionResult(boolean success, String result, String error) {
        public static ToolExecutionResult success(String result) {
            return new ToolExecutionResult(true, result, null);
        }

        public static ToolExecutionResult failure(String error) {
            return new ToolExecutionResult(false, null, error);
        }
    }
}
