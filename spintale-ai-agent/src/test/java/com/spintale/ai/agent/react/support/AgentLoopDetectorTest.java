package com.spintale.ai.agent.react.support;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AgentLoopDetector单元测试
 */
class AgentLoopDetectorTest {

    private AgentLoopDetector detector;

    @BeforeEach
    void setUp() {
        detector = new AgentLoopDetector(3);
    }

    @Test
    @DisplayName("检测器初始化状态正确")
    void testInitialState() {
        assertEquals(0, detector.getCallCount("any_tool"));
    }

    @Test
    @DisplayName("单次调用不触发循环检测")
    void testSingleCallNoLoop() {
        boolean isLoop = detector.checkAndRecord("tool1", "{\"arg\":\"value\"}");
        assertFalse(isLoop);
        assertEquals(1, detector.getCallCount("tool1"));
    }

    @Test
    @DisplayName("相同工具调用达到阈值触发循环检测")
    void testLoopDetection() {
        String toolName = "tool1";
        String args = "{\"arg\":\"value\"}";
        
        assertFalse(detector.checkAndRecord(toolName, args));
        assertFalse(detector.checkAndRecord(toolName, args));
        assertFalse(detector.checkAndRecord(toolName, args));
        assertTrue(detector.checkAndRecord(toolName, args));
        
        assertEquals(4, detector.getCallCount(toolName));
    }

    @Test
    @DisplayName("相同工具不同参数不触发精确循环检测")
    void testDifferentArgsNoExactLoop() {
        String toolName = "tool1";
        
        for (int i = 0; i < 5; i++) {
            detector.checkAndRecord(toolName, "{\"arg\":\"value" + i + "\"}");
        }
        
        assertEquals(5, detector.getCallCount(toolName));
    }

    @Test
    @DisplayName("相同工具相同参数第二次调用触发精确循环检测")
    void testExactLoopDetection() {
        String toolName = "tool1";
        String sameArgs = "{\"arg\":\"same\"}";
        
        assertFalse(detector.checkAndRecord(toolName, sameArgs));
        assertTrue(detector.checkAndRecord(toolName, sameArgs));
    }

    @Test
    @DisplayName("重置后检测器状态清空")
    void testReset() {
        detector.checkAndRecord("tool1", "{}");
        detector.checkAndRecord("tool2", "{}");
        
        detector.reset();
        
        assertEquals(0, detector.getCallCount("tool1"));
        assertEquals(0, detector.getCallCount("tool2"));
    }

    @Test
    @DisplayName("获取统计信息包含必要字段")
    void testGetStatistics() {
        detector.checkAndRecord("tool1", "{}");
        
        var stats = detector.getStatistics();
        
        assertTrue(stats.containsKey("toolCallCount"));
        assertTrue(stats.containsKey("threshold"));
        assertEquals(3, stats.get("threshold"));
    }

    @Test
    @DisplayName("不同工具调用独立计数")
    void testDifferentToolsIndependent() {
        assertFalse(detector.checkAndRecord("tool1", "{}"));
        assertFalse(detector.checkAndRecord("tool2", "{}"));
        
        assertEquals(1, detector.getCallCount("tool1"));
        assertEquals(1, detector.getCallCount("tool2"));
    }

    @Test
    @DisplayName("空参数不导致异常")
    void testNullArgs() {
        assertDoesNotThrow(() -> detector.checkAndRecord("tool1", null));
        assertDoesNotThrow(() -> detector.checkAndRecord("tool2", ""));
    }
}
