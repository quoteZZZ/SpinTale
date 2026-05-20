# SpinTale API使用指南

## 1. 快速开始

### 1.1 环境要求

- JDK 17+
- Maven 3.8+
- MySQL 8.0+
- Redis 6.0+
- Milvus 2.3+（可选）

### 1.2 配置环境变量

```bash
# 必需配置
export DB_PASSWORD=your_db_password
export REDIS_PASSWORD=your_redis_password
export TOKEN_SECRET=$(openssl rand -base64 32)

# AI配置（选择一个provider）
export DEEPSEEK_API_KEY=your_deepseek_key
# 或
export OPENAI_API_KEY=your_openai_key
```

### 1.3 启动应用

```bash
# 开发环境
java -jar spintale-admin.jar

# 生产环境
export SPRING_PROFILES_ACTIVE=prod,druid,ai
java -jar spintale-admin.jar
```

---

## 2. AI服务API

### 2.1 简单对话

```java
@Autowired
private AiFacade aiFacade;

// 简单对话
String response = aiFacade.chat("你好，请介绍一下Java");

// 带系统提示的对话
String response = aiFacade.chat(
    "你是一个Java专家",
    "请解释Spring Boot的优势"
);
```

### 2.2 RAG对话

```java
// RAG对话（带文档检索）
String response = aiFacade.chatWithRag(
    "请根据文档回答：如何配置数据库？",
    "docs-collection"  // 文档集合名称
);
```

### 2.3 Agent对话

```java
// Agent对话（带工具调用）
List<String> tools = Arrays.asList("web_search", "weather_query");
String response = aiFacade.chatWithAgent(
    "查询北京明天的天气",
    tools
);
```

### 2.4 流式对话

```java
// 流式输出
aiFacade.chatStreaming("讲一个故事", token -> {
    System.out.print(token);  // 实时输出每个token
});
```

---

## 3. 记忆管理API

### 3.1 会话记忆

```java
@Autowired
private MemoryStore memoryStore;

// 保存记忆
LongTermMemory memory = LongTermMemory.builder()
    .userId("user123")
    .content("用户的偏好信息")
    .type(LongTermMemory.MemoryType.FACT)
    .build();
memoryStore.save(memory);

// 查询用户记忆
List<LongTermMemory> memories = memoryStore.findByUserId("user123", 10);
```

### 3.2 两级缓存

```java
// 使用两级缓存记忆存储
TwoLevelMemoryStore cachedStore = new TwoLevelMemoryStore(
    redisStore,  // L2缓存
    10000,       // 最大容量
    30           // 过期时间（分钟）
);

// 查询时自动缓存
Optional<LongTermMemory> memory = cachedStore.findById("memory_id");

// 获取缓存统计
TwoLevelMemoryStore.CacheStats stats = cachedStore.getStats();
System.out.println("命中率: " + stats.localHitRate());
```

---

## 4. 工具系统API

### 4.1 注册自定义工具

```java
@Autowired
private ToolRegistry toolRegistry;

// 注册工具
toolRegistry.register(
    "calculator",
    "执行数学计算",
    args -> {
        double a = (double) args.get("a");
        double b = (double) args.get("b");
        String op = (String) args.get("operation");
        return switch (op) {
            case "add" -> String.valueOf(a + b);
            case "sub" -> String.valueOf(a - b);
            default -> "未知操作";
        };
    }
);
```

### 4.2 执行工具

```java
// 执行工具
ToolRegistry.ToolExecutionResult result = toolRegistry.execute(
    "calculator",
    Map.of("a", 10, "b", 5, "operation", "add"),
    Duration.ofSeconds(10)  // 超时时间
);

if (result.success()) {
    System.out.println("结果: " + result.result());
} else {
    System.err.println("错误: " + result.error());
}
```

---

## 5. RAG检索API

### 5.1 文档索引

```java
@Autowired
private DocumentIndexService indexService;

// 上传文档
DocumentIndexService.UploadDocument doc = new DocumentIndexService.UploadDocument();
doc.setContent("文档内容...");
doc.setMetadata(Map.of("source", "manual", "category", "tech"));

List<DocumentIndexService.IndexedDocument> indexed = 
    indexService.indexDocuments(List.of(doc), "my-collection");
```

### 5.2 向量检索

```java
@Autowired
private VectorRetrievalService retrievalService;

// 检索相关文档
List<RetrievalResult> results = retrievalService.retrieve(
    "如何配置数据库连接池",
    "my-collection",
    5  // top-k
);

for (RetrievalResult result : results) {
    System.out.println("内容: " + result.getContent());
    System.out.println("相似度: " + result.getScore());
}
```

---

## 6. Advisor拦截器

### 6.1 内置Advisor

| Advisor | 功能 | 顺序 |
|---------|------|------|
| MemoryAdvisor | 记忆管理 | 100 |
| RagAdvisor | RAG检索增强 | 200 |
| SemanticCacheAdvisor | 语义缓存 | 300 |
| SafetyAdvisor | 安全检查 | 400 |
| ObservabilityAdvisor | 可观测性 | 500 |

### 6.2 自定义Advisor

```java
@Component
public class CustomAdvisor implements Advisor {
    
    @Override
    public String getName() {
        return "custom";
    }
    
    @Override
    public int getOrder() {
        return 150;  // 在MemoryAdvisor之后
    }
    
    @Override
    public AdvisorRequest adviseRequest(AdvisorRequest request, AdvisorContext context) {
        // 请求预处理
        String query = request.getMessage();
        // 添加前缀
        return request.withMessage("【自定义前缀】" + query);
    }
    
    @Override
    public AdvisorResponse adviseResponse(AdvisorResponse response, AdvisorContext context) {
        // 响应后处理
        return response;
    }
}
```

---

## 7. Provider配置

### 7.1 DeepSeek配置

```yaml
spintale:
  ai:
    provider: openai-compatible
    openai-compatible:
      enabled: true
      api-key: ${DEEPSEEK_API_KEY}
      base-url: https://api.deepseek.com/v1
      model-name: deepseek-v4-flash
      temperature: 0.7
      max-tokens: 2000
```

### 7.2 OpenAI配置

```yaml
spintale:
  ai:
    provider: openai
    openai:
      enabled: true
      api-key: ${OPENAI_API_KEY}
      base-url: https://api.openai.com/v1
      model-name: gpt-4-turbo-preview
```

### 7.3 Ollama配置（本地模型）

```yaml
spintale:
  ai:
    provider: ollama
    ollama:
      enabled: true
      base-url: http://localhost:11434
      model-name: llama3.1
```

---

## 8. 弹性设计

### 8.1 熔断配置

```yaml
spintale:
  ai:
    resilience:
      circuit-breaker:
        enabled: true
        failure-rate-threshold: 50      # 失败率阈值50%
        slow-call-rate-threshold: 100   # 慢调用率阈值100%
        sliding-window-size: 100        # 滑动窗口大小
        wait-duration-in-open-state: 60s # 熔断等待时间
```

### 8.2 限流配置

```yaml
spintale:
  ai:
    resilience:
      rate-limiter:
        enabled: true
        limit-for-period: 10    # 每周期限制10次
        limit-refresh-period: 1s # 周期1秒
```

### 8.3 重试配置

```yaml
spintale:
  ai:
    resilience:
      retry:
        enabled: true
        max-attempts: 3          # 最大重试3次
        wait-duration: 1s        # 等待1秒
        exponential-backoff-multiplier: 2  # 指数退避倍数
```

---

## 9. 监控接口

### 9.1 Swagger文档

访问：`http://localhost:8080/swagger-ui.html`

### 9.2 Druid监控

访问：`http://localhost:8080/druid/`

用户名：`spintale`  
密码：`${DRUID_PASSWORD}`

### 9.3 健康检查

```bash
curl http://localhost:8080/actuator/health
```

---

## 10. 常量使用

### 10.1 线程池常量

```java
import com.spintale.ai.agent.constant.AgentPoolConstants;

ThreadPoolExecutor executor = new ThreadPoolExecutor(
    AgentPoolConstants.CORE_POOL_SIZE,      // 10
    AgentPoolConstants.MAX_POOL_SIZE,       // 50
    AgentPoolConstants.KEEP_ALIVE_SECONDS,  // 60
    TimeUnit.SECONDS,
    new LinkedBlockingQueue<>(AgentPoolConstants.QUEUE_CAPACITY)  // 1000
);
```

### 10.2 超时常量

```java
import com.spintale.ai.agent.constant.AgentTimeoutConstants;

long timeout = AgentTimeoutConstants.DEFAULT_TOOL_TIMEOUT_MS;  // 30000ms
int maxIterations = AgentTimeoutConstants.DEFAULT_MAX_ITERATIONS;  // 10
```

---

## 11. 安全最佳实践

### 11.1 配置加密

```bash
# 使用Jasypt加密密码
mvn jasypt:encrypt-value \
  -Djasypt.encryptor.password="master-key" \
  -Djasypt.plugin.value="your-password"
```

```yaml
# 使用加密后的密码
spring:
  data:
    redis:
      password: ENC(加密后的密文)
```

### 11.2 SQL注入防护

```java
import com.spintale.common.utils.sql.SqlInjectionProtector;

// 验证dataScope安全性
if (!SqlInjectionProtector.isDataScopeSafe(dataScope)) {
    throw new SecurityException("不安全的SQL片段");
}

// 清理dataScope
String safeScope = SqlInjectionProtector.sanitizeDataScope(dataScope);
```

### 11.3 权限控制

```java
// 方法级权限控制
@PreAuthorize("@ss.hasPermi('system:user:list')")
@GetMapping("/list")
public TableDataInfo list(SysUser user) {
    // 业务逻辑
}

// 数据权限过滤
@DataScope(deptAlias = "d", userAlias = "u")
public List<SysUser> selectUserList(SysUser user) {
    return userMapper.selectUserList(user);
}
```

---

## 12. 故障排查

### 12.1 查看日志

```bash
# 查看应用日志
tail -f logs/spintale.log

# 查看AI模块日志
tail -f logs/spintale.log | grep "AI"
```

### 12.2 常见问题

**问题1**: Token校验失败
```
解决：检查TOKEN_SECRET环境变量是否设置
```

**问题2**: AI调用失败
```
解决：
1. 检查API Key是否配置
2. 查看熔断器状态
3. 检查网络连接
```

**问题3**: 缓存未生效
```
解决：
1. 检查Redis连接
2. 确认缓存配置enabled=true
3. 查看缓存命中率统计
```

---

**文档版本**: 1.0  
**最后更新**: 2026-05-20
