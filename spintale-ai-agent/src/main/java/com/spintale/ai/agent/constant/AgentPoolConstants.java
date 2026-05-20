package com.spintale.ai.agent.constant;

/**
 * Agent线程池常量
 */
public final class AgentPoolConstants {
    
    private AgentPoolConstants() {}
    
    public static final int CORE_POOL_SIZE = 10;
    public static final int MAX_POOL_SIZE = 50;
    public static final long KEEP_ALIVE_SECONDS = 60L;
    public static final int QUEUE_CAPACITY = 1000;
    
    public static final int TOOL_CORE_POOL_SIZE = 10;
    public static final int TOOL_MAX_POOL_SIZE = 50;
    public static final int TOOL_QUEUE_CAPACITY = 500;
}
