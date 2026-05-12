# SpinTale AI 模块

基于 LangChain4j 构建的企业级 AI 智能模块，集成多种大语言模型和 AI 能力。

## 最新版本：v2.1

### 🆕 核心修复 (v2.1)
- **ReAct Agent**: 实现完整的 Reasoning + Acting 循环，支持真实工具调用
- **执行步骤追踪**: 详细记录每次工具调用的参数和结果
- **Token 统计**: 完整的 Token 使用量追踪
- **错误处理**: 增强的异常处理和超时控制

---

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

### 5. AI 内容生成 🆕
- **文章生成**: 自动撰写各类文章
- **小说创作**: 故事、小说、剧本创作
- **广告文案**: 广告语、营销文案生成
- **社交媒体**: 帖子、推文生成
- **产品描述**: 电商产品描述生成
- **邮件写作**: 商务邮件自动生成
- **博客文章**: 技术博客、观点文章
- **新闻稿**: 企业新闻稿生成
- **视频脚本**: 短视频、宣传片脚本
- **流式输出**: 支持 SSE 实时流式生成

### 6. 长期记忆管理 🆕
- **跨会话记忆**: 记住用户的重要信息和偏好
- **语义检索**: 基于向量相似度检索相关记忆
- **重要性评分**: 自动评估记忆的重要性
- **记忆压缩**: 将多个相关记忆合并为摘要
- **过期清理**: 自动清理过期或低价值记忆

### 7. 幻觉检测与缓解 🆕
- **事实一致性检查**: 检测 AI 回复是否与已知事实矛盾
- **置信度评估**: 实时评估 AI 回复的可信度
- **可疑模式识别**: 识别模糊引用、编造数据等模式
- **内部矛盾检测**: 发现自相矛盾的表述
- **警告提示**: 对低置信度回复添加警示标记

### 8. 增强型聊天服务 🆕
- **上下文优化**: 智能选择最相关的对话历史
- **记忆注入**: 将长期记忆动态注入系统提示
- **RAG 集成**: 结合检索内容进行生成
- **流式处理**: 支持实时流式响应

### 9. 可观测性
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
    
    # 上下文与记忆配置
    context:
      maxMessages: 20
      memoryRetrievalThreshold: 0.6
      longTermMemoryEnabled: true
    
    # 幻觉检测配置
    hallucinationDetection:
      enabled: true
      threshold: 0.5
      action: WARN  # WARN, REGENERATE, BLOCK
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

// 生成文章
GenerationResponse article = generationService.generateArticle(
    "人工智能的未来发展", 
    "AI, 技术，趋势", 
    "探讨 AI 技术的未来发展方向和应用场景"
);

// 生成广告词
GenerationResponse adCopy = generationService.generateAdCopy(
    "智能手表", 
    "健康监测、长续航、时尚设计", 
    "年轻白领"
);
```

## API 接口

### 内容生成接口

| 接口 | 方法 | 描述 |
|------|------|------|
| `/ai/generate/types` | GET | 获取支持的内容类型 |
| `/ai/generate/content` | POST | 通用内容生成接口 |
| `/ai/generate/article` | POST | 生成文章 |
| `/ai/generate/novel` | POST | 生成小说/故事 |
| `/ai/generate/ad-copy` | POST | 生成广告词 |
| `/ai/generate/marketing` | POST | 生成营销文案 |
| `/ai/generate/custom` | POST | 自定义内容生成 |
| `/ai/generate/stream` | GET | 流式生成 (SSE) |

### 使用示例

#### 生成文章

```bash
curl -X POST "http://localhost:8080/ai/generate/article" \
  -d "title=人工智能的未来" \
  -d "keywords=AI,技术，创新" \
  -d "description=探讨 AI 技术的发展趋势" \
  -d "length=medium" \
  -d "tone=专业"
```

#### 生成广告词

```bash
curl -X POST "http://localhost:8080/ai/generate/ad-copy" \
  -d "productName=智能音箱" \
  -d "productFeatures=语音控制、智能家居联动、高音质" \
  -d "targetAudience=科技爱好者" \
  -d "platform=电商"
```

#### 流式生成

```javascript
// 前端 SSE 示例
const eventSource = new EventSource(
  'http://localhost:8080/ai/generate/stream?contentType=article&title=AI 发展&description=文章内容'
);

eventSource.onmessage = (event) => {
  console.log('收到 token:', event.data);
};

eventSource.addEventListener('complete', (event) => {
  console.log('生成完成:', event.data);
  eventSource.close();
});

eventSource.addEventListener('error', (event) => {
  console.error('生成错误:', event.data);
  eventSource.close();
});
```

## 架构设计

```
spintale-ai/
├── core/               # 核心接口和数据模型
│   ├── AiChatService   # 聊天服务接口
│   ├── ChatRequest     # 聊天请求
│   ├── ChatResponse    # 聊天响应
│   └── EnhancedAiChatService  # 增强型聊天服务 🆕
├── client/             # 客户端实现
│   └── LangChainAiChatService  # LangChain4j 实现
├── config/             # 自动配置
│   ├── AiProperties    # 配置属性
│   ├── AiOpenAiAutoConfig
│   ├── AiOllamaAutoConfig
│   ├── AiGenerationAutoConfig  # 内容生成配置 🆕
│   └── AiEnhancedAutoConfig    # 增强功能配置 🆕
├── tool/               # 工具系统
│   ├── AiTool          # 工具接口
│   └── WeatherTool     # 示例工具
├── memory/             # 对话记忆 🆕
│   ├── ConversationManager       # 会话管理器
│   ├── ConversationSession       # 会话实体
│   ├── ConversationMessage       # 消息实体
│   ├── InMemoryConversationManager
│   ├── LongTermMemory            # 长期记忆实体 🆕
│   ├── LongTermMemoryManager     # 长期记忆接口 🆕
│   └── InMemoryLongTermMemoryManager  # 长期记忆实现 🆕
├── hallucination/      # 幻觉检测 🆕
│   └── HallucinationDetectionService  # 幻觉检测服务
├── retrieval/          # RAG 检索
│   ├── RetrievalService
│   └── EmbeddingRetrievalService
├── generation/         # 内容生成 🆕
│   ├── model/          # 数据模型
│   │   ├── ContentType           # 内容类型枚举
│   │   ├── GenerationRequest     # 生成请求
│   │   └── GenerationResponse    # 生成响应
│   ├── template/       # Prompt 模板
│   │   ├── ContentTemplate       # 模板接口
│   │   ├── ArticleTemplate       # 文章模板
│   │   ├── NovelTemplate         # 小说模板
│   │   └── AdCopyTemplate        # 广告词模板
│   └── service/        # 服务层
│       └── ContentGenerationService  # 内容生成服务
└── web/                # Web 控制器 🆕
    └── controller/
        └── ContentGenerationController  # 内容生成 API
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

### 自定义内容模板

```java
@Component
public class BlogPostTemplate implements ContentTemplate {
    
    @Override
    public String getContentType() {
        return "blog_post";
    }
    
    @Override
    public String getSystemPrompt() {
        return "你是一位资深技术博主，擅长撰写深入浅出、通俗易懂的技术文章。";
    }
    
    @Override
    public String buildPrompt(Map<String, Object> params) {
        StringBuilder prompt = new StringBuilder();
        String title = (String) params.get("title");
        String description = (String) params.get("description");
        
        prompt.append("请写一篇技术博客：").append(title).append("\n\n");
        if (description != null) {
            prompt.append("要求：\n").append(description);
        }
        
        return prompt.toString();
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
