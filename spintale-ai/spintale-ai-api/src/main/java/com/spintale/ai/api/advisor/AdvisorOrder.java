package com.spintale.ai.api.advisor;

/**
 * Advisor execution order constants.
 * Lower values execute first in the request phase, last in the response phase.
 * 
 * Order sequence (request phase):
 * 1. SafetyAdvisor (100) - Security and validation checks
 * 2. SemanticCacheAdvisor (200) - Check for cached responses
 * 3. MemoryAdvisor (300) - Inject conversation history
 * 4. RagAdvisor (400) - Inject retrieved context
 * 5. HallucinationAdvisor (500) - Detect hallucinations
 * 6. ObservabilityAdvisor (600) - Metrics and tracing
 */
public final class AdvisorOrder {
    
    private AdvisorOrder() {
    }
    
    public static final int SAFETY = 100;
    
    public static final int SEMANTIC_CACHE = 200;
    
    public static final int MEMORY = 300;
    
    public static final int RAG = 400;
    
    public static final int HALLUCINATION = 500;
    
    public static final int OBSERVABILITY = 600;
    
    public static final int CUSTOM_MIN = 50;
    
    public static final int CUSTOM_MAX = 1000;
}
