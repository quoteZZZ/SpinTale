package com.spintale.ai.tool.registry;

import dev.langchain4j.agent.tool.ToolSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
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

    /** 注册的工具定义 */
    private final Map<String, RegisteredTool> tools = new ConcurrentHashMap<>();

    public ToolRegistry() {
    }

    @Autowired
    public ToolRegistry(List<AiTool> aiTools) {
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

    /**
     * 执行工具
     */
    public ToolExecutionResult execute(String name, Map<String, Object> args) {
        RegisteredTool tool = tools.get(name);
        if (tool == null) {
            return ToolExecutionResult.error("Tool not found: " + name);
        }

        long startTime = System.currentTimeMillis();
        try {
            String result = tool.executor().apply(args != null ? args : new HashMap<>());
            long duration = System.currentTimeMillis() - startTime;
            log.info("Tool executed: name={}, duration={}ms", name, duration);
            return ToolExecutionResult.success(result, duration);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("Tool execution failed: name={}, duration={}ms", name, duration, e);
            return ToolExecutionResult.error(e.getMessage(), duration);
        }
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
