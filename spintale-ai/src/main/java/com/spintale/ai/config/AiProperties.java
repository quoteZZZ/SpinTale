package com.spintale.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AI 配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "spintale.ai")
public class AiProperties {
    
    /**
     * 是否启用 AI 功能
     */
    private Boolean enabled = false;
    
    /**
     * 模型提供商：openai, azure, ollama, anthropic
     */
    private String provider = "openai";
    
    /**
     * 默认模型名称
     */
    private String model = "gpt-3.5-turbo";
    
    /**
     * OpenAI 配置
     */
    private OpenAiConfig openai = new OpenAiConfig();
    
    /**
     * Azure OpenAI 配置
     */
    private AzureOpenAiConfig azure = new AzureOpenAiConfig();
    
    /**
     * Ollama 配置 (本地模型)
     */
    private OllamaConfig ollama = new OllamaConfig();
    
    /**
     * Anthropic 配置
     */
    private AnthropicConfig anthropic = new AnthropicConfig();
    
    /**
     * RAG 配置
     */
    private RagConfig rag = new RagConfig();
    
    /**
     * 上下文管理配置
     */
    private ContextConfig context = new ContextConfig();
    
    /**
     * 幻觉检测配置
     */
    private HallucinationDetectionConfig hallucinationDetection = new HallucinationDetectionConfig();
    
    @Data
    public static class ContextConfig {
        /**
         * 最大上下文消息数
         */
        private Integer maxMessages = 20;
        
        /**
         * 记忆检索相似度阈值
         */
        private Double memoryRetrievalThreshold = 0.6;
        
        /**
         * 是否启用长期记忆
         */
        private Boolean longTermMemoryEnabled = true;
    }
    
    @Data
    public static class HallucinationDetectionConfig {
        /**
         * 是否启用幻觉检测
         */
        private Boolean enabled = true;
        
        /**
         * 幻觉判定阈值（低于此值认为是幻觉）
         */
        private Double threshold = 0.5;
        
        /**
         * 检测到幻觉时的处理方式：WARN(警告), REGENERATE(重新生成), BLOCK(阻止)
         */
        private String action = "WARN";
    }
    
    @Data
    public static class OpenAiConfig {
        /**
         * API Key
         */
        private String apiKey;
        
        /**
         * API 端点
         */
        private String baseUrl = "https://api.openai.com/v1";
        
        /**
         * 超时时间 (毫秒)
         */
        private Long timeout = 60000L;
    }
    
    @Data
    public static class AzureOpenAiConfig {
        /**
         * API Key
         */
        private String apiKey;
        
        /**
         * Endpoint
         */
        private String endpoint;
        
        /**
         * Deployment Name
         */
        private String deploymentName;
        
        /**
         * API Version
         */
        private String apiVersion = "2024-02-15-preview";
    }
    
    @Data
    public static class OllamaConfig {
        /**
         * Base URL
         */
        private String baseUrl = "http://localhost:11434";
        
        /**
         * 模型名称
         */
        private String model = "llama2";
    }
    
    @Data
    public static class AnthropicConfig {
        /**
         * API Key
         */
        private String apiKey;
        
        /**
         * API 端点
         */
        private String baseUrl = "https://api.anthropic.com";
        
        /**
         * 默认模型
         */
        private String model = "claude-3-sonnet-20240229";
    }
    
    @Data
    public static class RagConfig {
        /**
         * 是否启用 RAG
         */
        private Boolean enabled = false;
        
        /**
         * 向量存储类型：milvus, chroma, elasticsearch
         */
        private String vectorStore = "milvus";
        
        /**
         * Embedding 模型
         */
        private String embeddingModel = "bge-small-en-v1.5";
        
        /**
         * Milvus 配置
         */
        private MilvusConfig milvus = new MilvusConfig();
    }
    
    @Data
    public static class MilvusConfig {
        /**
         * 连接地址
         */
        private String uri = "http://localhost:19530";
        
        /**
         * 用户名
         */
        private String username;
        
        /**
         * 密码
         */
        private String password;
        
        /**
         * 集合名称
         */
        private String collectionName = "spintale_knowledge";
    }
}
