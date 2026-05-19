package com.spintale.ai.infrastructure.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Central AI configuration.
 */
@Data
@Component
@ConfigurationProperties(prefix = "spintale.ai")
public class AiProperties {

    private Boolean enabled = false;
    private String provider = "openai";
    private String model = "gpt-4o-mini";

    private ProviderProperties.OpenAiConfig openai = new ProviderProperties.OpenAiConfig();
    private ProviderProperties.OpenAiCompatibleConfig openaiCompatible = new ProviderProperties.OpenAiCompatibleConfig();
    private ProviderProperties.AzureOpenAiConfig azure = new ProviderProperties.AzureOpenAiConfig();
    private ProviderProperties.OllamaConfig ollama = new ProviderProperties.OllamaConfig();
    private ProviderProperties.AnthropicConfig anthropic = new ProviderProperties.AnthropicConfig();
    private ProviderProperties.LocalModelConfig localModel = new ProviderProperties.LocalModelConfig();
    private ProviderProperties.RoutingConfig routing = new ProviderProperties.RoutingConfig();

    private RagProperties rag = new RagProperties();

    private CapabilityProperties.ContextConfig context = new CapabilityProperties.ContextConfig();
    private CapabilityProperties.HallucinationDetectionConfig hallucinationDetection =
            new CapabilityProperties.HallucinationDetectionConfig();
    private CapabilityProperties.SafetyConfig safety = new CapabilityProperties.SafetyConfig();
    private CapabilityProperties.SemanticCacheConfig semanticCache = new CapabilityProperties.SemanticCacheConfig();
    private CapabilityProperties.ExperimentConfig experiment = new CapabilityProperties.ExperimentConfig();

    private AgentProperties agent = new AgentProperties();
    private WorkflowProperties workflow = new WorkflowProperties();
    private ToolProperties tools = new ToolProperties();
    private McpProperties mcp = new McpProperties();
}
