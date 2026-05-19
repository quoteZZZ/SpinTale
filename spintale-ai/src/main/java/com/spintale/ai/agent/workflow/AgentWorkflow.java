package com.spintale.ai.agent.workflow;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import com.spintale.ai.agent.react.AgentResult;
import com.spintale.ai.generation.model.GenerationRequest;
import com.spintale.ai.generation.model.GenerationResponse;
import com.spintale.ai.capability.hallucination.HallucinationDetectionResult;

/**
 * Temporal Workflow interface for AI Agent orchestration.
 * 
 * This workflow coordinates the multi-step ReAct agent execution:
 * 1. Receive user input
 * 2. Retrieve relevant context from vector database
 * 3. Execute agent reasoning loop (Think -> Act -> Observe)
 * 4. Detect hallucinations
 * 5. Generate final response
 * 
 * @author SpinTale AI Team
 */
@WorkflowInterface
public interface AgentWorkflow {
    
    /**
     * Main workflow method to execute an AI agent conversation.
     * 
     * @param request The generation request containing user input and context
     * @return The generated response with agent reasoning trace
     */
    @WorkflowMethod
    GenerationResponse executeAgent(GenerationRequest request);
    
    /**
     * Signal method to cancel ongoing agent execution.
     * 
     * @param conversationId The conversation identifier to cancel
     */
    @WorkflowMethod
    void cancelExecution(String conversationId);
}
