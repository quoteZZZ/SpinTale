package com.spintale.ai.agent.react.support;

import dev.langchain4j.model.output.TokenUsage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AgentTokenTracker单元测试
 */
class AgentTokenTrackerTest {

    private AgentTokenTracker tracker;

    @BeforeEach
    void setUp() {
        tracker = new AgentTokenTracker();
    }

    @Test
    @DisplayName("初始状态Token为0")
    void testInitialState() {
        TokenUsage usage = tracker.getTotalUsage();
        
        assertEquals(0, usage.inputTokenCount());
        assertEquals(0, usage.outputTokenCount());
        assertEquals(0, usage.totalTokenCount());
    }

    @Test
    @DisplayName("累加Token使用量")
    void testAccumulate() {
        TokenUsage usage1 = new TokenUsage(100, 50, 150);
        TokenUsage usage2 = new TokenUsage(200, 100, 300);
        
        tracker.accumulate(usage1);
        tracker.accumulate(usage2);
        
        TokenUsage total = tracker.getTotalUsage();
        assertEquals(300, total.inputTokenCount());
        assertEquals(150, total.outputTokenCount());
        assertEquals(450, total.totalTokenCount());
    }

    @Test
    @DisplayName("累加null不影响结果")
    void testAccumulateNull() {
        tracker.accumulate(null);
        
        TokenUsage usage = tracker.getTotalUsage();
        assertEquals(0, usage.inputTokenCount());
    }

    @Test
    @DisplayName("记录迭代Token使用")
    void testRecordIteration() {
        TokenUsage usage1 = new TokenUsage(100, 50, 150);
        TokenUsage usage2 = new TokenUsage(200, 100, 300);
        
        tracker.recordIteration(1, usage1);
        tracker.recordIteration(2, usage2);
        
        TokenUsage total = tracker.getTotalUsage();
        assertEquals(300, total.inputTokenCount());
        assertEquals(150, total.outputTokenCount());
        
        var stats = tracker.getStatistics();
        assertEquals(2, stats.get("iterations"));
    }

    @Test
    @DisplayName("重置后Token清零")
    void testReset() {
        tracker.accumulate(new TokenUsage(100, 50, 150));
        tracker.reset();
        
        TokenUsage usage = tracker.getTotalUsage();
        assertEquals(0, usage.inputTokenCount());
        assertEquals(0, usage.outputTokenCount());
        assertEquals(0, usage.totalTokenCount());
    }

    @Test
    @DisplayName("获取统计信息包含必要字段")
    void testGetStatistics() {
        tracker.accumulate(new TokenUsage(100, 50, 150));
        
        var stats = tracker.getStatistics();
        
        assertEquals(100L, stats.get("inputTokens"));
        assertEquals(50L, stats.get("outputTokens"));
        assertEquals(150L, stats.get("totalTokens"));
        assertTrue(stats.containsKey("iterations"));
    }

    @Test
    @DisplayName("转换为SpintaleTokenUsage格式正确")
    void testToSpintaleTokenUsage() {
        tracker.accumulate(new TokenUsage(100, 50, 150));
        
        com.spintale.ai.core.model.TokenUsage usage = tracker.toSpintaleTokenUsage();
        
        assertEquals(100, usage.inputTokenCount());
        assertEquals(50, usage.outputTokenCount());
        assertEquals(150, usage.totalTokenCount());
    }

    @Test
    @DisplayName("多次累加正确累加")
    void testMultipleAccumulates() {
        for (int i = 0; i < 10; i++) {
            tracker.accumulate(new TokenUsage(10, 5, 15));
        }
        
        TokenUsage total = tracker.getTotalUsage();
        assertEquals(100, total.inputTokenCount());
        assertEquals(50, total.outputTokenCount());
        assertEquals(150, total.totalTokenCount());
    }
}
