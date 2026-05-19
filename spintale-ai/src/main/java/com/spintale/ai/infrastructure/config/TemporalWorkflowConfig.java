package com.spintale.ai.infrastructure.config;

import com.spintale.ai.agent.workflow.AgentActivities;
import com.spintale.ai.agent.workflow.AgentWorkflowImpl;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Temporal 工作流引擎自动配置
 * 
 * 当满足以下条件时自动配置：
 * 1. classpath 中存在 Temporal SDK
 * 2. spintale.ai.workflow.enabled=true
 */
@Configuration
@ConditionalOnClass(WorkflowClient.class)
@ConditionalOnProperty(prefix = "spintale.ai.workflow", name = "enabled", havingValue = "true")
public class TemporalWorkflowConfig {

    @Value("${spintale.ai.workflow.target-server:http://localhost:7233}")
    private String temporalServer;

    @Value("${spintale.ai.workflow.task-queue:ai-agent-queue}")
    private String taskQueue;

    @Bean
    public WorkflowServiceStubs workflowServiceStubs() {
        WorkflowServiceStubsOptions options = WorkflowServiceStubsOptions.newBuilder()
            .setTarget(temporalServer)
            .build();
        return WorkflowServiceStubs.newInstance(options);
    }

    @Bean
    public WorkflowClient workflowClient(WorkflowServiceStubs stubs) {
        WorkflowClientOptions options = WorkflowClientOptions.newBuilder()
            .build();
        return WorkflowClient.newInstance(stubs, options);
    }

    @Bean
    public WorkerFactory workerFactory(WorkflowClient client) {
        return WorkerFactory.newInstance(client);
    }

    /**
     * AI Agent 工作流 Worker
     * 注册工作流实现和活动实现到指定的任务队列
     */
    @Bean
    public Worker agentWorker(
            WorkerFactory factory,
            AgentActivities activities,
            AgentWorkflowImpl workflow) {
        
        Worker worker = factory.newWorker(taskQueue);
        
        // 注册工作流实现
        worker.registerWorkflowImplementationTypes(
            AgentWorkflowImpl.class
        );
        
        // 注册活动实现
        worker.registerActivitiesImplementations(activities);
        
        return worker;
    }
}
