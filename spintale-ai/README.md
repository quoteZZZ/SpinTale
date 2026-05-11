# SpinTale AI 模块

基于 LangChain4j 构建的企业级 AI 智能模块，集成多种大语言模型和 AI 能力。

## 核心特性

### 1. 多模型支持
- **OpenAI**: GPT-3.5, GPT-4, GPT-4o 等
- **Azure OpenAI**: 企业级 Azure 部署
- **Ollama**: 本地模型运行 (Llama2, Mistral, Qwen 等)
- **Anthropic**: Claude 系列模型

### 2. 对话管理
- 会话上下文维护
- 多轮对话历史
- 消息记忆管理

### 3. 工具/函数调用
- 动态工具注册
- 自动参数解析
- 可扩展工具系统

### 4. RAG (检索增强生成)
- 向量数据库集成 (Milvus, Chroma, Elasticsearch)
- 文档加载和分块
- 语义检索

### 5. 可观测性
- Token 使用统计
- 请求/响应日志
- 性能监控

## 快速开始

### 1. 添加依赖

在 `pom.xml` 中添加:

```xml
<dependency>
    <groupId>com.spintale</groupId>
    <artifactId>spintale-ai</artifactId>
    <version>3.9.2</version>
</dependency>
```

### 2. 配置应用

在 `application.yml` 中配置:

```yaml
spintale:
  ai:
    enabled: true
    provider: openai  # 或 ollama, azure, anthropic
    model: gpt-3.5-turbo
    
    openai:
      api-key: ${OPENAI_API_KEY}
      baseUrl: https://api.openai.com/v1
      timeout: 60000
      
    # Ollama 本地模型配置
    ollama:
      baseUrl: http://localhost:11434
      model: llama2
      
    # RAG 配置
    rag:
      enabled: false
      vectorStore: milvus
      embeddingModel: bge-small-en-v1.5
      milvus:
        uri: http://localhost:19530
        collectionName: spintale_knowledge
```

### 3. 使用示例

```java
@Autowired
private AiChatService aiChatService;

// 简单聊天
String response = aiChatService.chat("你好，请介绍一下你自己");

// 带上下文的聊天
ChatRequest request = ChatRequest.builder()
    .sessionId("session-123")
    .message("继续刚才的话题")
    .systemPrompt("你是一个专业的助手")
    .temperature(0.7)
    .maxTokens(2048)
    .build();
    
ChatResponse response = aiChatService.chat(request);
System.out.println(response.getContent());
```

## 架构设计

```
spintale-ai/
├── core/               # 核心接口和数据模型
│   ├── AiChatService   # 聊天服务接口
│   ├── ChatRequest     # 聊天请求
│   ├── ChatResponse    # 聊天响应
│   └── ...
├── client/             # 客户端实现
│   └── LangChainAiChatService  # LangChain4j 实现
├── config/             # 自动配置
│   ├── AiProperties    # 配置属性
│   ├── AiOpenAiAutoConfig
│   └── AiOllamaAutoConfig
├── tool/               # 工具系统
│   ├── AiTool          # 工具接口
│   └── WeatherTool     # 示例工具
├── memory/             # 对话记忆
│   ├── ConversationManager
│   └── ConversationSession
└── retrieval/          # RAG 检索 (待实现)
```

## 扩展开发

### 自定义工具

```java
@Component
public class CustomTool implements AiTool {
    
    @Override
    public String getName() {
        return "my_custom_tool";
    }
    
    @Override
    public String getDescription() {
        return "我的自定义工具描述";
    }
    
    @Override
    public String execute(String args) {
        // 实现工具逻辑
        return "执行结果";
    }
}
```

### 自定义模型提供商

实现 `ChatLanguageModel` 接口并创建对应的自动配置类。

## 最佳实践

1. **API Key 安全**: 使用环境变量存储敏感信息
2. **限流控制**: 实现请求限流避免 API 超额
3. **错误处理**: 完善的异常处理和重试机制
4. **缓存策略**: 对高频请求实现缓存
5. **监控告警**: 集成 Prometheus/Grafana 监控

## 参考资源

- [LangChain4j 官方文档](https://docs.langchain4j.dev/)
- [OpenAI API 文档](https://platform.openai.com/docs)
- [Ollama 官网](https://ollama.ai/)
