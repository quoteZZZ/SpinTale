package com.spintale.ai.agent.router;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 本地小模型管理器
 * 支持 Qwen2.5-7B, Phi-3-mini, TinyLlama 等轻量级模型
 * 用于处理简单查询，降低成本和延迟
 *
 * 从根目录 src/ 迁移到 spintale-ai 模块，包名统一
 */
@Slf4j
@Service
public class LocalSmallModelService {

    @Value("${spintale.ai.small-model.enabled:true}")
    private boolean smallModelEnabled;

    @Value("${spintale.ai.small-model.provider:ollama}")
    private String provider;

    @Value("${spintale.ai.small-model.endpoint:http://localhost:11434}")
    private String endpoint;

    @Value("${spintale.ai.small-model.model-name:qwen2.5:7b}")
    private String modelName;

    private ChatModel chatModel;
    private EmbeddingModel embeddingModel;

    public String getModelName() {
        return modelName;
    }

    /**
     * 初始化本地小模型
     */
    public void initialize() {
        if (!smallModelEnabled) {
            log.info("本地小模型已禁用");
            return;
        }

        log.info("初始化本地小模型：provider={}, model={}, endpoint={}", provider, modelName, endpoint);

        try {
            switch (provider.toLowerCase()) {
                case "ollama" -> initOllama();
                case "vllm" -> initVllm();
                case "llama.cpp" -> initLlamaCpp();
                default -> {
                    log.warn("未知的小模型提供商：{}, 使用默认 Ollama", provider);
                    initOllama();
                }
            }
            log.info("本地小模型初始化成功：{}", modelName);
        } catch (Exception e) {
            log.error("本地小模型初始化失败，将回退到大模型：{}", e.getMessage());
            this.chatModel = null;
        }
    }

    private void initOllama() {
        // Note: OllamaChatModel and OllamaEmbeddingModel are not available in LangChain4j 1.13.1
        // Using OpenAI-compatible API instead
        this.chatModel = dev.langchain4j.model.openai.OpenAiChatModel.builder()
                .baseUrl(endpoint + "/v1")
                .apiKey("ollama")
                .modelName(modelName)
                .temperature(0.7)
                .timeout(java.time.Duration.ofSeconds(30))
                .build();

        // Embedding model initialization is commented out as OllamaEmbeddingModel is not available
        // this.embeddingModel = dev.langchain4j.model.embedding.OllamaEmbeddingModel.builder()...

        log.info("Ollama small model ready (using OpenAI-compatible API): {} @ {}", modelName, endpoint);
    }

    private void initVllm() {
        this.chatModel = dev.langchain4j.model.openai.OpenAiChatModel.builder()
                .baseUrl(endpoint + "/v1")
                .apiKey("ollama")
                .modelName(modelName)
                .temperature(0.7)
                .timeout(java.time.Duration.ofSeconds(30))
                .build();
        log.info("vLLM 小模型就绪：{} @ {}", modelName, endpoint);
    }

    private void initLlamaCpp() {
        this.chatModel = dev.langchain4j.model.openai.OpenAiChatModel.builder()
                .baseUrl(endpoint + "/v1")
                .apiKey("no-key")
                .modelName(modelName)
                .temperature(0.7)
                .timeout(java.time.Duration.ofSeconds(30))
                .build();
        log.info("llama.cpp 小模型就绪：{} @ {}", modelName, endpoint);
    }

    public ChatModel getChatModel() {
        return smallModelEnabled ? chatModel : null;
    }

    public EmbeddingModel getEmbeddingModel() {
        return smallModelEnabled ? embeddingModel : null;
    }

    public boolean isAvailable() {
        return smallModelEnabled && chatModel != null;
    }

    public HealthStatus healthCheck() {
        if (!smallModelEnabled) return new HealthStatus(false, "DISABLED", "小模型功能已禁用");
        if (chatModel == null) return new HealthStatus(false, "NOT_INITIALIZED", "小模型未初始化");
        try {
            // Use chat method instead of generate
            var response = chatModel.chat("你好");
            return response != null && !response.toString().isEmpty()
                    ? new HealthStatus(true, "HEALTHY", "小模型响应正常")
                    : new HealthStatus(false, "NO_RESPONSE", "小模型无响应");
        } catch (Exception e) {
            return new HealthStatus(false, "ERROR", "小模型调用失败：" + e.getMessage());
        }
    }

    public record HealthStatus(boolean healthy, String status, String message) {}
}
