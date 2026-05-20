package com.spintale.ai.agent.tool.registry;

import com.alibaba.fastjson2.JSON;
import dev.langchain4j.agent.tool.ToolSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;

/**
 * 工具注册中心
 *
 * 统一管理所有 AI Agent 可用的工具：
 * - 注册/注销工具
 * - 工具发现与列表
 * - 工具参数校验
 * - 工具执行（带权限控制和超时）
 *
 * 参考 LangChain4j 的 @Tool 注解模式，但提供更灵活的运行时注册
 */
@Service
public class ToolRegistry {

    private static final Logger log = LoggerFactory.getLogger(ToolRegistry.class);

    private final Map<String, RegisteredTool> tools = new ConcurrentHashMap<>();
    
    private final ExecutorService executorService;
    
    @Value("${spintale.ai.tool.default-timeout-ms:30000}")
    private long defaultTimeoutMs = 30000;
    
    @Value("${spintale.ai.tool.enabled-permission-check:false}")
    private boolean permissionCheckEnabled = false;
    
    private final Map<String, Set<String>> toolPermissions = new ConcurrentHashMap<>();

    public ToolRegistry() {
        this.executorService = new ThreadPoolExecutor(
                10, 50, 60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(500),
                r -> {
                    Thread t = new Thread(r, "tool-executor");
                    t.setDaemon(true);
                    return t;
                },
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }

    @Autowired
    public ToolRegistry(List<AiTool> aiTools) {
        this();
        if (aiTools != null) {
            aiTools.stream()
                    .filter(tool -> tool != null && tool.isEnabled())
                    .forEach(tool -> register(tool.getName(), tool.getDescription(), tool::execute, null));
        }
    }

    /**
     * 注册工具
     *
     * @param name        工具名称
     * @param description 工具描述（LLM 会看到此描述来决定是否调用）
     * @param executor    工具执行函数
     * @param paramSchema 参数 JSON Schema（可选，用于参数校验）
     */
    public void register(String name, String description,
                          Function<Map<String, Object>, String> executor,
                          String paramSchema) {
        Objects.requireNonNull(name, "Tool name cannot be null");
        Objects.requireNonNull(executor, "Tool executor cannot be null");

        RegisteredTool tool = new RegisteredTool(name, description, executor, paramSchema);
        tools.put(name, tool);
        log.info("Registered tool: {} - {}", name, description);
    }

    /**
     * 注册简单工具（无参数校验）
     */
    public void register(String name, String description, Function<Map<String, Object>, String> executor) {
        register(name, description, executor, null);
    }

    /**
     * 注销工具
     */
    public void unregister(String name) {
        RegisteredTool removed = tools.remove(name);
        if (removed != null) {
            log.info("Unregistered tool: {}", name);
        }
    }

    /**
     * 获取工具
     */
    public RegisteredTool getTool(String name) {
        return tools.get(name);
    }

    public ToolExecutionResult execute(String name, Map<String, Object> args) {
        return execute(name, args, Duration.ofMillis(defaultTimeoutMs), null);
    }
    
    public ToolExecutionResult execute(String name, Map<String, Object> args, Duration timeout) {
        return execute(name, args, timeout, null);
    }
    
    public ToolExecutionResult execute(String name, Map<String, Object> args, String userId) {
        return execute(name, args, Duration.ofMillis(defaultTimeoutMs), userId);
    }
    
    public ToolExecutionResult execute(String name, Map<String, Object> args, Duration timeout, String userId) {
        RegisteredTool tool = tools.get(name);
        if (tool == null) {
            return ToolExecutionResult.error("Tool not found: " + name);
        }
        
        if (permissionCheckEnabled && userId != null) {
            if (!hasPermission(name, userId)) {
                log.warn("Permission denied: user={}, tool={}", userId, name);
                return ToolExecutionResult.error("Permission denied for tool: " + name);
            }
        }
        
        if (tool.paramSchema() != null && args != null) {
            try {
                validateParameters(args, tool.paramSchema());
            } catch (Exception e) {
                return ToolExecutionResult.error("Parameter validation failed: " + e.getMessage());
            }
        }
        
        long startTime = System.currentTimeMillis();
        try {
            Map<String, Object> finalArgs = args != null ? args : new HashMap<>();
            
            CompletableFuture<String> future = CompletableFuture.supplyAsync(
                    () -> tool.executor().apply(finalArgs),
                    executorService
            );
            
            String result = future.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
            long duration = System.currentTimeMillis() - startTime;
            
            log.info("Tool executed: name={}, duration={}ms, user={}", name, duration, userId);
            return ToolExecutionResult.success(result, duration);
            
        } catch (TimeoutException e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("Tool execution timeout: name={}, timeout={}ms", name, timeout.toMillis());
            return ToolExecutionResult.error("Execution timeout after " + timeout.toMillis() + "ms", duration);
        } catch (ExecutionException e) {
            long duration = System.currentTimeMillis() - startTime;
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            log.error("Tool execution failed: name={}, duration={}ms", name, duration, cause);
            return ToolExecutionResult.error(cause.getMessage(), duration);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("Tool execution failed: name={}, duration={}ms", name, duration, e);
            return ToolExecutionResult.error(e.getMessage(), duration);
        }
    }
    
    private void validateParameters(Map<String, Object> args, String schema) {
        if (schema == null || schema.isBlank()) {
            return;
        }
        
        try {
            JSON.parseObject(JSON.toJSONString(args));
        } catch (Exception e) {
            log.warn("JSON Schema validation failed: schema={}, args={}", schema, args, e);
            throw new IllegalArgumentException("Parameter validation failed: " + e.getMessage(), e);
        }
    }
    
    public void grantPermission(String toolName, String userId) {
        toolPermissions.computeIfAbsent(toolName, k -> ConcurrentHashMap.newKeySet()).add(userId);
        log.debug("Granted permission: tool={}, user={}", toolName, userId);
    }
    
    public void revokePermission(String toolName, String userId) {
        Set<String> users = toolPermissions.get(toolName);
        if (users != null) {
            users.remove(userId);
        }
        log.debug("Revoked permission: tool={}, user={}", toolName, userId);
    }
    
    public boolean hasPermission(String toolName, String userId) {
        Set<String> allowedUsers = toolPermissions.get(toolName);
        return allowedUsers == null || allowedUsers.contains(userId);
    }

    /**
     * 获取所有工具名称
     */
    public Set<String> getToolNames() {
        return Collections.unmodifiableSet(tools.keySet());
    }

    /**
     * 获取所有工具规格（用于 LLM function calling）
     */
    public List<ToolSpecification> getToolSpecifications() {
        return tools.values().stream()
                .map(RegisteredTool::toSpecification)
                .toList();
    }

    /**
     * 获取工具描述（用于 ReAct prompt）
     */
    public String getToolsDescription() {
        StringBuilder sb = new StringBuilder();
        for (RegisteredTool tool : tools.values()) {
            sb.append("- ").append(tool.name()).append(": ").append(tool.description()).append("\n");
        }
        return sb.toString();
    }

    /**
     * 检查工具是否存在
     */
    public boolean hasTool(String name) {
        return tools.containsKey(name);
    }

    /**
     * 获取工具数量
     */
    public int size() {
        return tools.size();
    }

    // ==================== 内部数据模型 ====================

    /**
     * 注册的工具记录
     */
    public record RegisteredTool(
            String name,
            String description,
            Function<Map<String, Object>, String> executor,
            String paramSchema
    ) {
        public ToolSpecification toSpecification() {
            return ToolSpecification.builder()
                    .name(name)
                    .description(description != null ? description : "")
                    .build();
        }
    }

    /**
     * 工具执行结果
     */
    public record ToolExecutionResult(
            boolean success,
            String result,
            String error,
            long durationMs
    ) {
        public static ToolExecutionResult success(String result, long durationMs) {
            return new ToolExecutionResult(true, result, null, durationMs);
        }

        public static ToolExecutionResult error(String error, long durationMs) {
            return new ToolExecutionResult(false, null, error, durationMs);
        }

        public static ToolExecutionResult error(String error) {
            return new ToolExecutionResult(false, null, error, 0);
        }
    }
}
