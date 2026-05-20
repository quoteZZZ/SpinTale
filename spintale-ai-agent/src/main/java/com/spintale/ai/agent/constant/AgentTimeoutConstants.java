package com.spintale.ai.agent.constant;

/**
 * Agent超时常量
 */
public final class AgentTimeoutConstants {
    
    private AgentTimeoutConstants() {}
    
    public static final long DEFAULT_TOOL_TIMEOUT_MS = 30000;
    public static final long DEFAULT_AGENT_TIMEOUT_SECONDS = 300;
    public static final int DEFAULT_MAX_ITERATIONS = 10;
    public static final int DEFAULT_LOOP_DETECTION_THRESHOLD = 3;
}
