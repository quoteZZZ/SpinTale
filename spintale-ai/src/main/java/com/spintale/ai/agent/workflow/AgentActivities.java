package com.spintale.ai.agent.workflow;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import com.spintale.ai.generation.model.GenerationRequest;
import com.spintale.ai.generation.model.GenerationResponse;
import com.spintale.ai.retrieval.embedding.RetrievalResult;
import com.spintale.ai.agent.react.AgentResult;
import com.spintale.ai.capability.hallucination.HallucinationDetectionResult;
import java.util.List;

/**
 * Temporal Activities interface for AI Agent operations.
 * 
 * Each activity represents a discrete, retryable operation:
 * - Context retrieval from vector database
 * - LLM inference
 * - Tool execution
 * - Hallucination detection
 * - Response generation
 * 
 * @author SpinTale AI Team
 */
@ActivityInterface
public interface AgentActivities {
    
    /**
     * Retrieve relevant context from vector database based on user query.
     * 
     * @param query The user's question or input
     * @param topK Number of results to retrieve
     * @return Retrieval result with matching documents and scores
     */
    @ActivityMethod
    RetrievalResult retrieveContext(String query, int topK);
    
    /**
     * Call LLM to generate a response or reasoning step.
     * 
     * @param request Generation request with prompt and parameters
     * @return LLM response with generated text and token usage
     */
    @ActivityMethod
    GenerationResponse callLLM(GenerationRequest request);
    
    /**
     * Execute a tool/function based on agent's decision.
     * 
     * @param toolName Name of the tool to execute
     * @param toolArgs Arguments for the tool
     * @return Tool execution result
     */
    @ActivityMethod
    AgentResult.ToolExecutionResult executeTool(String toolName, String toolArgs);
    
    /**
     * Check response for hallucinations and factual inconsistencies.
     * 
     * @param response The generated response to verify
     * @param context Retrieved context for fact-checking
     * @return Hallucination detection result with confidence score
     */
    @ActivityMethod
    HallucinationDetectionResult detectHallucination(GenerationResponse response, List<String> context);
    
    /**
     * Generate final response after all reasoning steps are complete.
     * 
     * @param request Original generation request
     * @param reasoningTrace Complete trace of agent reasoning steps
     * @param verifiedContext Verified context from retrieval
     * @return Final polished response
     */
    @ActivityMethod
    GenerationResponse generateFinalResponse(
        GenerationRequest request,
        String reasoningTrace,
        List<String> verifiedContext
    );
    
    /**
     * Save conversation to long-term memory.
     * 
     * @param conversationId Unique conversation identifier
     * @param userId User identifier
     * @param request Original request
     * @param response Generated response
     */
    @ActivityMethod
    void saveToMemory(String conversationId, String userId, GenerationRequest request, GenerationResponse response);
}
