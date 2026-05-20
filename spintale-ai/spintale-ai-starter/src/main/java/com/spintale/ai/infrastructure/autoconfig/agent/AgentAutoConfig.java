package com.spintale.ai.infrastructure.autoconfig.agent;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.spintale.ai.agent.react.api.AgentService;
import com.spintale.ai.agent.react.impl.ReActAgent;
import com.spintale.ai.infrastructure.properties.AiProperties;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Auto configuration for local agent implementations.
 */
@Configuration
@ConditionalOnClass(ReActAgent.class)
@ConditionalOnProperty(prefix = "spintale.ai.agent.react", name = "enabled", havingValue = "true", matchIfMissing = true)
public class AgentAutoConfig {

    @Bean
    @ConditionalOnBean(ChatModel.class)
    @ConditionalOnMissingBean(AgentService.class)
    public AgentService reactAgent(
            ChatModel chatModel,
            ObjectProvider<StreamingChatModel> streamingChatModel,
            @Qualifier("toolFunctions") Map<String, Function<Map<String, Object>, String>> toolFunctions,
            @Qualifier("toolSpecifications") List<ToolSpecification> toolSpecifications,
            AiProperties properties) {
        var config = properties.getAgent().getReact();
        int maxIterations = config.getMaxIterations() == null ? 10 : config.getMaxIterations();
        long toolTimeoutMs = config.getToolTimeoutMs() == null ? 30000L : config.getToolTimeoutMs();
        int loopThreshold = config.getLoopThreshold() == null ? 3 : config.getLoopThreshold();

        return new ReActAgent(
                chatModel,
                streamingChatModel.getIfAvailable(),
                toolFunctions,
                toolSpecifications,
                maxIterations)
                .setToolExecutionTimeoutMs(toolTimeoutMs)
                .setLoopDetectionThreshold(loopThreshold);
    }
}
