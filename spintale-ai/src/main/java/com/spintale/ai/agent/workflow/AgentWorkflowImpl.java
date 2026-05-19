package com.spintale.ai.agent.workflow;

import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.Workflow;
import com.spintale.ai.generation.model.GenerationRequest;
import com.spintale.ai.generation.model.GenerationResponse;
import com.spintale.ai.retrieval.embedding.RetrievalResult;
import com.spintale.ai.agent.react.AgentResult;
import com.spintale.ai.capability.hallucination.HallucinationDetectionResult;
import java.util.List;
import java.util.ArrayList;

/**
 * Implementation of AgentWorkflow using Temporal.
 * 
 * This workflow orchestrates the complete AI agent execution:
 * 1. Retrieve context from vector database (Milvus)
 * 2. Execute ReAct reasoning loop with tool calls
 * 3. Detect and filter hallucinations
 * 4. Generate final response
 * 5. Save to long-term memory
 * 
 * Features:
 * - Automatic retry on failures with exponential backoff
 * - State persistence for long-running conversations
 * - Versioning support for seamless updates
 * - Complete execution trace for debugging
 * 
 * @author SpinTale AI Team
 */
@WorkflowImpl(taskQueues = {"ai-agent-queue"})
public class AgentWorkflowImpl implements AgentWorkflow {
    
    private final AgentActivities activities = Workflow.newActivityStub(
        AgentActivities.class
    );
    
    @Override
    public GenerationResponse executeAgent(GenerationRequest request) {
        String conversationId = request.getConversationId();
        String userId = request.getUserId();
        String query = request.getPrompt();
        
        // Step 1: Retrieve relevant context from vector database
        RetrievalResult retrievalResult = activities.retrieveContext(query, 5);
        List<String> contextDocuments = extractionDocuments(retrievalResult);
        
        // Step 2: Execute ReAct reasoning loop
        StringBuilder reasoningTrace = new StringBuilder();
        GenerationResponse intermediateResponse = null;
        
        int maxIterations = 10;
        int iteration = 0;
        
        while (iteration < maxIterations) {
            // Build prompt with context and reasoning history
            GenerationRequest llmRequest = buildReasoningRequest(
                query, 
                contextDocuments, 
                reasoningTrace.toString(),
                iteration
            );
            
            // Call LLM for reasoning step
            intermediateResponse = activities.callLLM(llmRequest);
            reasoningTrace.append(intermediateResponse.getContent());
            
            // Check if LLM wants to execute a tool
            if (intermediateResponse.requiresToolExecution()) {
                String toolName = intermediateResponse.getToolName();
                String toolArgs = intermediateResponse.getToolArgs() != null ? 
                    intermediateResponse.getToolArgs().toString() : "{}";
                
                AgentResult.ToolExecutionResult toolResult = activities.executeTool(
                    toolName,
                    toolArgs
                );
                reasoningTrace.append("\nObservation: ").append(toolResult.getResult());
            } else {
                // Reasoning complete, proceed to final response
                break;
            }
            
            iteration++;
        }
        
        // Step 3: Detect hallucinations in the response
        HallucinationDetectionResult hallucinationCheck = activities.detectHallucination(
            intermediateResponse,
            contextDocuments
        );
        
        // If high hallucination risk, regenerate with stricter constraints
        GenerationResponse finalResponse;
        if (hallucinationCheck.getConfidenceScore() < 0.7) {
            finalResponse = activities.generateFinalResponse(
                request,
                reasoningTrace.toString(),
                filterHighConfidenceDocuments(contextDocuments, hallucinationCheck)
            );
        } else {
            finalResponse = intermediateResponse;
        }
        
        // Step 4: Save conversation to long-term memory (async)
        Workflow.newDetachedCancellationScope(() -> {
            activities.saveToMemory(conversationId, userId, request, finalResponse);
        }).run();
        
        // Add metadata to response
        finalResponse.setReasoningTrace(reasoningTrace.toString());
        finalResponse.setRetrievedContext(contextDocuments);
        finalResponse.setHallucinationScore(hallucinationCheck.getConfidenceScore());
        finalResponse.setIterations(iteration);
        
        return finalResponse;
    }
    
    @Override
    public void cancelExecution(String conversationId) {
        // Temporal will handle cancellation gracefully
        Workflow.sleep(java.time.Duration.ofSeconds(1));
    }
    
    // Helper methods
    
    private List<String> extractionDocuments(RetrievalResult result) {
        List<String> documents = new ArrayList<>();
        if (result != null && result.getDocuments() != null) {
            for (var doc : result.getDocuments()) {
                documents.add(doc.getContent());
            }
        }
        return documents;
    }
    
    private GenerationRequest buildReasoningRequest(
        String query,
        List<String> context,
        String reasoningHistory,
        int iteration
    ) {
        GenerationRequest request = new GenerationRequest();
        request.setPrompt(buildReActPrompt(query, context, reasoningHistory, iteration));
        request.setTemperature(0.7f);
        request.setMaxTokens(1000);
        return request;
    }
    
    private String buildReActPrompt(String query, List<String> context, String history, int iteration) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are an AI assistant using ReAct (Reasoning + Acting) framework.\n\n");
        prompt.append("Context from knowledge base:\n");
        for (int i = 0; i < context.size(); i++) {
            prompt.append(String.format("[%d] %s\n", i+1, context.get(i)));
        }
        prompt.append("\n");
        
        if (!history.isEmpty()) {
            prompt.append("Previous reasoning steps:\n").append(history).append("\n\n");
        }
        
        prompt.append("User question: ").append(query).append("\n\n");
        prompt.append("Think step by step. If you need to use a tool, output:\n");
        prompt.append("Thought: [your reasoning]\n");
        prompt.append("Action: [tool_name]\n");
        prompt.append("Action Input: {\"arg\": \"value\"}\n");
        prompt.append("Otherwise, output your final answer.\n");
        
        return prompt.toString();
    }
    
    private List<String> filterHighConfidenceDocuments(
        List<String> documents,
        HallucinationDetectionResult detection
    ) {
        // Filter documents based on hallucination detection feedback
        // In production, this would use more sophisticated logic
        return documents.subList(0, Math.min(3, documents.size()));
    }
}
