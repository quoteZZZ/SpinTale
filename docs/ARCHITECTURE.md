# SpinTale 架构设计文档

## 1. 项目概述

SpinTale是一个基于Spring Boot 4.0.3和JDK 17的企业级应用，集成了RuoYi框架和自定义AI框架，提供完整的业务管理系统和AI能力。

### 1.1 技术栈

| 类型 | 技术 | 版本 |
|------|------|------|
| 核心框架 | Spring Boot | 4.0.3 |
| JDK | OpenJDK | 17 |
| ORM框架 | MyBatis | 4.0.1 |
| 数据库连接池 | Druid | 1.2.28 |
| 缓存 | Redis + Caffeine | - |
| AI框架 | LangChain4j | 1.13.1 |
| 向量数据库 | Milvus | 2.5.8 |
| 工作流引擎 | Temporal | 1.35.0 |
| 弹性设计 | Resilience4j | 2.2.0 |

---

## 2. 模块架构

### 2.1 模块结构

```
SpinTale (父模块)
├── spintale-admin      # Web服务入口
├── spintale-common     # 通用工具层
├── spintale-framework  # 框架核心层
├── spintale-system     # 系统业务层
└── spintale-ai         # AI能力层
    ├── spintale-ai-core        # 核心抽象
    ├── spintale-ai-api         # 流式API
    ├── spintale-ai-agent       # Agent编排
    ├── spintale-ai-retrieval   # RAG检索
    ├── spintale-ai-providers   # Provider实现
    └── spintale-ai-starter     # 自动配置
```

### 2.2 模块职责

#### spintale-admin
- Web服务启动入口
- 控制器层（Controller）
- API接口定义
- 监控配置（Swagger、Druid）

#### spintale-common
- 通用工具类（DateUtils、StringUtils等）
- 常量定义
- 异常体系
- Redis缓存工具
- 安全工具

#### spintale-framework
- Spring Security安全配置
- 数据源配置（Druid）
- Web配置
- 拦截器和过滤器
- AOP切面

#### spintale-system
- 用户、角色、菜单管理
- 部门、岗位管理
- 字典、配置管理
- 日志、监控

#### spintale-ai
- AI核心能力（对话、向量检索、Agent）
- 多Provider支持（OpenAI、Ollama等）
- 记忆管理、工具系统
- RAG检索、流式处理

---

## 3. 核心设计

### 3.1 安全架构

```
请求 → JWT过滤器 → 权限校验 → 业务处理 → 响应
           ↓
      Token解析验证
           ↓
      用户信息注入
```

**安全组件**：
- `JwtAuthenticationTokenFilter` - JWT Token过滤器
- `SecurityConfig` - 安全配置
- `SqlInjectionProtector` - SQL注入防护
- `XssFilter` - XSS攻击防护

### 3.2 AI架构

```
用户请求 → AiFacade → AdvisorChain → ChatModel → Provider → LLM
              ↓            ↓
         记忆管理     拦截器链
              ↓            ↓
         缓存层      RAG检索
```

**核心组件**：
- `AiFacade` - AI统一门面
- `AdvisorChain` - 拦截器链（记忆、RAG、缓存、熔断）
- `ReActAgent` - Agent编排
- `ToolRegistry` - 工具注册中心
- `TwoLevelMemoryStore` - 两级缓存记忆存储

### 3.3 数据访问

```
Controller → Service → Mapper → 数据库
    ↓                    ↓
 参数校验           数据权限过滤
```

**数据权限**：
- 基于`dataScope`实现数据权限过滤
- 使用`SqlInjectionProtector`防止SQL注入
- 支持多租户数据隔离

---

## 4. 设计模式应用

### 4.1 创建型模式

| 模式 | 应用位置 | 说明 |
|------|---------|------|
| Builder | ChatClient、ChatOptions | 流式API构建 |
| Factory | ServiceProxyFactory、ToolRegistry | 服务实例创建 |
| 单例 | Spring Bean | 依赖注入管理 |

### 4.2 结构型模式

| 模式 | 应用位置 | 说明 |
|------|---------|------|
| 适配器 | LangChain4jAdapter、MilvusVectorStore | 统一接口适配 |
| 装饰器 | AdvisorChain | 功能增强链 |
| 代理 | AiServiceRegistrar | 动态代理 |

### 4.3 行为型模式

| 模式 | 应用位置 | 说明 |
|------|---------|------|
| 责任链 | AdvisorChain | 请求处理链 |
| 策略 | 多Provider切换 | 算法策略选择 |
| 观察者 | AgentCallback | 事件回调 |
| 模板方法 | PromptTemplate | 模板渲染 |

---

## 5. 性能设计

### 5.1 线程池配置

```java
// Agent工具执行线程池
ThreadPoolExecutor executor = new ThreadPoolExecutor(
    10,   // 核心线程数
    50,   // 最大线程数
    60L,  // 空闲存活时间
    TimeUnit.SECONDS,
    new LinkedBlockingQueue<>(1000),  // 有界队列
    new ThreadPoolExecutor.CallerRunsPolicy()  // 拒绝策略
);
```

### 5.2 缓存策略

**两级缓存架构**：
```
请求 → L1缓存(Caffeine) → L2缓存(Redis) → 数据库
          ↓                    ↓
      本地内存            分布式缓存
      毫秒级              十毫秒级
```

**缓存配置**：
- L1缓存：最大容量10000，过期30分钟
- L2缓存：Redis，过期60分钟
- 命中策略：先L1后L2，异步回写

### 5.3 连接池配置

```yaml
# Druid数据库连接池
initialSize: 5
minIdle: 10
maxActive: 20
maxWait: 60000

# Redis连接池
lettuce:
  pool:
    min-idle: 0
    max-idle: 8
    max-active: 8
```

---

## 6. 可观测性设计

### 6.1 日志体系

```java
// 统一日志格式
log.info("Operation: {}, UserId: {}, Duration: {}ms", 
    operation, userId, duration);

// 异常日志
log.error("Operation failed: {}", operation, exception);
```

**日志级别**：
- DEBUG：详细调试信息
- INFO：关键操作记录
- WARN：警告信息（非关键异常）
- ERROR：错误信息（关键异常）

### 6.2 监控指标

**关键指标**：
- 请求QPS和响应时间
- AI调用次数和Token消耗
- 缓存命中率
- 线程池状态

**监控组件**：
- Druid监控台：`/druid/*`
- Springdoc：`/swagger-ui.html`
- OpenTelemetry：分布式追踪

---

## 7. 扩展点设计

### 7.1 新增AI Provider

```java
// 1. 实现接口
public class CustomProvider extends AbstractProvider<CustomConfig> {
    @Override
    public ChatModel createChatModel() { ... }
}

// 2. 添加配置
@ConfigurationProperties(prefix = "spintale.ai.custom")
public class CustomProperties { ... }

// 3. 自动配置
@Configuration
@ConditionalOnProperty(prefix = "spintale.ai.custom", name = "enabled")
class CustomAutoConfig { ... }
```

### 7.2 新增Advisor

```java
@Component
public class CustomAdvisor implements Advisor {
    @Override
    public AdvisorRequest adviseRequest(AdvisorRequest request, AdvisorContext context) {
        // 请求预处理
        return request;
    }
    
    @Override
    public AdvisorResponse adviseResponse(AdvisorResponse response, AdvisorContext context) {
        // 响应后处理
        return response;
    }
}
```

### 7.3 新增工具

```java
@Tool(name = "custom_tool", description = "自定义工具")
public class CustomTool implements AiTool {
    @Override
    public String execute(Map<String, Object> args) {
        // 工具实现
        return result;
    }
}
```

---

## 8. 配置管理

### 8.1 多环境配置

```
application.yml           # 公共配置
application-dev.yml       # 开发环境
application-test.yml      # 测试环境
application-prod.yml      # 生产环境
application-druid.yml     # 数据源配置
application-ai.yml        # AI配置
```

### 8.2 配置验证

```java
@Component
public class AiConfigValidator implements ApplicationListener<ApplicationReadyEvent> {
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        // 启动时验证配置完整性
        validateProviderConfig();
        validateSecurityConfig();
    }
}
```

### 8.3 敏感配置

**支持的配置方式**：
1. 环境变量注入：`${ENV_VAR:default}`
2. Jasypt加密：`ENC(加密密文)`
3. 配置中心：Vault、Nacos

---

## 9. 安全设计

### 9.1 认证授权

```
登录 → JWT Token生成 → Token返回客户端
          ↓
客户端携带Token → 服务端验证 → 授权访问
```

### 9.2 数据安全

- **密码加密**：BCrypt加密存储
- **SQL防注入**：SqlInjectionProtector验证
- **XSS防护**：XssFilter过滤
- **敏感数据脱敏**：日志脱敏

### 9.3 API安全

- API Key环境变量注入
- IP白名单控制
- 用户配额限制
- 请求频率限制

---

## 10. 部署架构

### 10.1 单机部署

```
Nginx → SpinTale应用 → MySQL
           ↓
         Redis
           ↓
        Milvus
```

### 10.2 集群部署

```
Nginx负载均衡
    ↓
[SpinTale实例1] [SpinTale实例2] [SpinTale实例3]
    ↓              ↓              ↓
Redis Cluster (主从)
    ↓
MySQL (主从分离)
    ↓
Milvus Cluster
```

### 10.3 容器化部署

```yaml
# Kubernetes部署
apiVersion: apps/v1
kind: Deployment
metadata:
  name: spintale
spec:
  replicas: 3
  template:
    spec:
      containers:
      - name: spintale
        image: spintale:3.9.2
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod,druid,ai"
```

---

## 11. 常量定义

### 11.1 线程池常量

```java
public final class AgentPoolConstants {
    public static final int CORE_POOL_SIZE = 10;
    public static final int MAX_POOL_SIZE = 50;
    public static final long KEEP_ALIVE_SECONDS = 60L;
    public static final int QUEUE_CAPACITY = 1000;
}
```

### 11.2 超时常量

```java
public final class AgentTimeoutConstants {
    public static final long DEFAULT_TOOL_TIMEOUT_MS = 30000;
    public static final long DEFAULT_AGENT_TIMEOUT_SECONDS = 300;
    public static final int DEFAULT_MAX_ITERATIONS = 10;
}
```

---

## 12. 测试策略

### 12.1 单元测试

```java
@Test
@DisplayName("验证核心功能")
void testCoreFunctionality() {
    // Given
    String input = "test";
    
    // When
    String result = service.process(input);
    
    // Then
    assertNotNull(result);
}
```

### 12.2 测试覆盖目标

- 核心工具类：90%+
- 业务服务类：80%+
- 控制器类：70%+
- 整体覆盖率：80%+

---

**文档版本**: 1.0  
**最后更新**: 2026-05-20  
**维护者**: SpinTale Team
