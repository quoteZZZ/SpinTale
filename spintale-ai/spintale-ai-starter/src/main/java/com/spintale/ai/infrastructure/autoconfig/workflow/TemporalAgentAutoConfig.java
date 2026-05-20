package com.spintale.ai.infrastructure.autoconfig.workflow;

import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.spintale.ai.agent.workflow.AgentActivities;
import com.spintale.ai.agent.workflow.AgentActivitiesImpl;
import com.spintale.ai.agent.workflow.AgentWorkflowImpl;
import com.spintale.ai.capability.hallucination.HallucinationDetector;
import com.spintale.ai.capability.memory.api.LongTermMemoryManager;
import com.spintale.ai.core.api.AiChatService;
import com.spintale.ai.infrastructure.properties.AiProperties;
import com.spintale.ai.retrieval.vector.RetrievalService;
import com.spintale.ai.tool.registry.AiTool;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;

/**
 * Auto configuration for Temporal-backed AI agent workflows.
 */
@Configuration
@ConditionalOnClass(WorkflowClient.class)
@ConditionalOnProperty(prefix = "spintale.ai.workflow", name = "enabled", havingValue = "true")
public class TemporalAgentAutoConfig {

    private final AiProperties properties;

    public TemporalAgentAutoConfig(AiProperties properties) {
        this.properties = properties;
    }

    @Bean
    public WorkflowServiceStubs workflowServiceStubs() {
        WorkflowServiceStubsOptions options = WorkflowServiceStubsOptions.newBuilder()
                .setTarget(properties.getWorkflow().getTargetServer())
                .build();
        return WorkflowServiceStubs.newInstance(options);
    }

    @Bean
    public WorkflowClient workflowClient(WorkflowServiceStubs stubs) {
        WorkflowClientOptions options = WorkflowClientOptions.newBuilder().build();
        return WorkflowClient.newInstance(stubs, options);
    }

    @Bean
    public WorkerFactory workerFactory(WorkflowClient client) {
        return WorkerFactory.newInstance(client);
    }

    @Bean
    @ConditionalOnBean({RetrievalService.class, AiChatService.class, HallucinationDetector.class, LongTermMemoryManager.class})
    public AgentActivities agentActivities(
            RetrievalService retrievalService,
            AiChatService chatService,
            List<AiTool> tools,
            HallucinationDetector hallucinationService,
            LongTermMemoryManager memoryManager) {
        return new AgentActivitiesImpl(retrievalService, chatService, tools, hallucinationService, memoryManager);
    }

    @Bean
    @ConditionalOnBean(AgentActivities.class)
    public Worker agentWorker(WorkerFactory factory, AgentActivities activities) {
        Worker worker = factory.newWorker(properties.getWorkflow().getTaskQueue());
        worker.registerWorkflowImplementationTypes(AgentWorkflowImpl.class);
        worker.registerActivitiesImplementations(activities);
        return worker;
    }
}
