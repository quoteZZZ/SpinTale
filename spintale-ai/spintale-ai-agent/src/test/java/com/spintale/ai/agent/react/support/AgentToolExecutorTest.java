package com.spintale.ai.agent.react.support;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AgentToolExecutor单元测试
 */
class AgentToolExecutorTest {

    private AgentToolExecutor executor;

    @BeforeEach
    void setUp() {
        executor = new AgentToolExecutor();
    }

    @Test
    @DisplayName("执行简单工具返回正确结果")
    void testExecuteSimpleTool() {
        Function<Map<String, Object>, String> tool = args -> "result: " + args.get("input");
        
        AgentToolExecutor.ToolExecutionResult result = executor.execute(
            "testTool",
            "{\"input\":\"hello\"}",
            tool
        );
        
        assertTrue(result.success());
        assertEquals("result: hello", result.result());
        assertNull(result.error());
    }

    @Test
    @DisplayName("工具返回null转换为默认值")
    void testToolReturnsNull() {
        Function<Map<String, Object>, String> tool = args -> null;
        
        AgentToolExecutor.ToolExecutionResult result = executor.execute(
            "nullTool",
            "{}",
            tool
        );
        
        assertTrue(result.success());
        assertEquals("(tool returned no value)", result.result());
    }

    @Test
    @DisplayName("工具不存在返回失败")
    void testToolNotFound() {
        AgentToolExecutor.ToolExecutionResult result = executor.execute(
            "nonExistent",
            "{}",
            null
        );
        
        assertFalse(result.success());
        assertNotNull(result.error());
        assertTrue(result.error().contains("not found"));
    }

    @Test
    @DisplayName("工具抛出异常返回失败结果")
    void testToolThrowsException() {
        Function<Map<String, Object>, String> tool = args -> {
            throw new RuntimeException("Tool error");
        };
        
        AgentToolExecutor.ToolExecutionResult result = executor.execute(
            "errorTool",
            "{}",
            tool
        );
        
        assertFalse(result.success());
        assertNotNull(result.error());
        assertTrue(result.error().contains("failed"));
    }

    @Test
    @DisplayName("空JSON参数解析为空Map")
    void testEmptyJsonArgs() {
        Function<Map<String, Object>, String> tool = args -> {
            assertTrue(args.isEmpty());
            return "ok";
        };
        
        AgentToolExecutor.ToolExecutionResult result = executor.execute(
            "testTool",
            "",
            tool
        );
        
        assertTrue(result.success());
    }

    @Test
    @DisplayName("无效JSON参数解析为空Map")
    void testInvalidJsonArgs() {
        Function<Map<String, Object>, String> tool = args -> {
            assertTrue(args.isEmpty());
            return "ok";
        };
        
        AgentToolExecutor.ToolExecutionResult result = executor.execute(
            "testTool",
            "invalid json",
            tool
        );
        
        assertTrue(result.success());
    }

    @Test
    @DisplayName("复杂JSON参数正确解析")
    void testComplexJsonArgs() {
        Function<Map<String, Object>, String> tool = args -> {
            assertEquals("value1", args.get("key1"));
            assertEquals(123, args.get("key2"));
            return "parsed";
        };
        
        AgentToolExecutor.ToolExecutionResult result = executor.execute(
            "testTool",
            "{\"key1\":\"value1\",\"key2\":123}",
            tool
        );
        
        assertTrue(result.success());
        assertEquals("parsed", result.result());
    }

    @Test
    @DisplayName("工具执行超时返回失败")
    void testToolTimeout() {
        Function<Map<String, Object>, String> slowTool = args -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "should not reach here";
        };
        
        AgentToolExecutor.ToolExecutionResult result = executor.execute(
            "slowTool",
            "{}",
            slowTool,
            100
        );
        
        assertFalse(result.success());
        assertTrue(result.error().contains("timeout"));
    }

    @Test
    @DisplayName("关闭执行器不抛出异常")
    void testShutdown() {
        assertDoesNotThrow(() -> executor.shutdown());
    }
}
