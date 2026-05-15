package com.spintale.ai.model.local;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.qna.QuestionAnsweringModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 本地小模型管理器
 * 支持 Qwen2.5-7B, Phi-3-mini, TinyLlama 等轻量级模型
 * 用于处理简单查询，降低成本和延迟
 */
@Slf4j
@Service
public class LocalSmallModelService {

    @Value("${ai.small-model.enabled:true}")
    private boolean smallModelEnabled;

    @Value("${ai.small-model.provider:ollama}")
    private String provider; // ollama, llama.cpp, vllm

    @Value("${ai.small-model.endpoint:http://localhost:11434}")
    private String endpoint;

    @Value("${ai.small-model.model-name:qwen2.5:7b}")
    private String modelName;

    /**
     * 获取当前配置的模型名称
     */
    public String getModelName() {
        return modelName;
    }

    private ChatLanguageModel chatModel;
    private EmbeddingModel embeddingModel;

    /**
     * 初始化本地小模型
     * 根据配置选择不同的推理后端
     */
    public void initialize() {
        if (!smallModelEnabled) {
            log.info("本地小模型已禁用");
            return;
        }

        log.info("初始化本地小模型：provider={}, model={}, endpoint={}", 
                 provider, modelName, endpoint);

        try {
            switch (provider.toLowerCase()) {
                case "ollama":
                    initOllama();
                    break;
                case "vllm":
                    initVllm();
                    break;
                case "llama.cpp":
                    initLlamaCpp();
                    break;
                default:
                    log.warn("未知的小模型提供商：{}, 使用默认 Ollama", provider);
                    initOllama();
            }
            log.info("本地小模型初始化成功：{}", modelName);
        } catch (Exception e) {
            log.error("本地小模型初始化失败，将回退到大模型：{}", e.getMessage());
            this.chatModel = null;
        }
    }

    /**
     * 初始化 Ollama 后端
     * 推荐模型：qwen2.5:7b, phi3:mini, tinyllama
     */
    private void initOllama() {
        // LangChain4j 内置 Ollama 支持
        this.chatModel = dev.langchain4j.model.chat.OllamaChatModel.builder()
                .baseUrl(endpoint)
                .modelName(modelName)
                .temperature(0.7)
                .timeout(java.time.Duration.ofSeconds(30))
                .build();

        this.embeddingModel = dev.langchain4j.model.embedding.OllamaEmbeddingModel.builder()
                .baseUrl(endpoint)
                .modelName("nomic-embed-text") // 或 mxbai-embed-large
                .build();

        log.info("Ollama 小模型就绪：{} @ {}", modelName, endpoint);
    }

    /**
     * 初始化 vLLM 后端（高性能推理）
     */
    private void initVllm() {
        // vLLM 兼容 OpenAI API
        this.chatModel = dev.langchain4j.model.openai.OpenAiChatModel.builder()
                .baseUrl(endpoint + "/v1")
                .apiKey("ollama") // vLLM 不需要真实 key
                .modelName(modelName)
                .temperature(0.7)
                .timeout(java.time.Duration.ofSeconds(30))
                .build();

        log.info("vLLM 小模型就绪：{} @ {}", modelName, endpoint);
    }

    /**
     * 初始化 llama.cpp 后端
     */
    private void initLlamaCpp() {
        // llama.cpp server 也兼容 OpenAI API
        this.chatModel = dev.langchain4j.model.openai.OpenAiChatModel.builder()
                .baseUrl(endpoint + "/v1")
                .apiKey("no-key")
                .modelName(modelName)
                .temperature(0.7)
                .timeout(java.time.Duration.ofSeconds(30))
                .build();

        log.info("llama.cpp 小模型就绪：{} @ {}", modelName, endpoint);
    }

    /**
     * 获取聊天模型
     * @return ChatLanguageModel 或 null（如果未启用）
     */
    public ChatLanguageModel getChatModel() {
        if (!smallModelEnabled || chatModel == null) {
            return null;
        }
        return chatModel;
    }

    /**
     * 获取嵌入模型
     */
    public EmbeddingModel getEmbeddingModel() {
        if (!smallModelEnabled || embeddingModel == null) {
            return null;
        }
        return embeddingModel;
    }

    /**
     * 检查小模型是否可用
     */
    public boolean isAvailable() {
        return smallModelEnabled && chatModel != null;
    }

    /**
     * 健康检查
     */
    public HealthStatus healthCheck() {
        if (!smallModelEnabled) {
            return new HealthStatus(false, "DISABLED", "小模型功能已禁用");
        }
        if (chatModel == null) {
            return new HealthStatus(false, "NOT_INITIALIZED", "小模型未初始化或初始化失败");
        }
        
        // 简单测试
        try {
            var response = chatModel.generate("你好");
            if (response != null && !response.isEmpty()) {
                return new HealthStatus(true, "HEALTHY", "小模型响应正常");
            } else {
                return new HealthStatus(false, "NO_RESPONSE", "小模型无响应");
            }
        } catch (Exception e) {
            return new HealthStatus(false, "ERROR", "小模型调用失败：" + e.getMessage());
        }
    }

    public record HealthStatus(boolean healthy, String status, String message) {}
}
