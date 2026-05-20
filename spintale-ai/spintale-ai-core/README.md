# spintale-ai-core 模块

## 📦 模块概述

SpinTale AI 核心抽象层，提供数据模型、SPI 接口和基础工具类。

## 🏗️ 包结构

```
com.spintale.ai.core/
├── model/              # 数据模型
│   ├── ChatMessage     # 聊天消息
│   ├── ChatRequest     # 聊天请求
│   ├── ChatResponse    # 聊天响应
│   └── TokenUsage      # Token 统计
├── spi/                # SPI 接口
│   ├── ChatModel       # 聊天模型接口
│   ├── EmbeddingModel  # 嵌入模型接口
│   ├── ModelProvider   # 模型提供者接口
│   ├── ModelProviderRegistry  # 提供者注册中心
│   └── ModelRouter     # 智能路由器
├── options/            # 配置选项
│   └── ChatOptions     # 聊天配置选项
├── exception/          # 异常体系
│   └── AiServiceException  # AI 服务异常
├── util/               # 工具类
│   └── JsonUtils       # JSON 工具
├── constant/           # 常量定义
│   └── AiConstants     # AI 常量
└── package-info.java   # 模块文档
```

## ✅ 已完成功能

### 1. 数据模型（model）

- **ChatMessage**: 支持 system/user/assistant/tool 角色
- **ChatRequest**: 包含消息列表、模型配置、流式标志
- **ChatResponse**: 包含回复内容、Token 统计、完成原因
- **TokenUsage**: 输入/输出/总计 Token 数，支持累加

### 2. SPI 接口（spi）

#### ChatModel
```java
public interface ChatModel {
    ChatResponse chat(ChatRequest request);
    void streamChat(ChatRequest request, StreamHandler handler);
    String getProviderId();
    String getModelName();
}
```

#### EmbeddingModel
```java
public interface EmbeddingModel {
    List<Float> embed(String text);
    List<List<Float>> embedAll(List<String> texts);
    String getProviderId();
    int getDimension();
}
```

#### ModelProvider
```java
public interface ModelProvider {
    String getId();
    ChatModel getChatModel();
    EmbeddingModel getEmbeddingModel();
    boolean supportsModel(String modelName);
    int getPriority();
}
```

### 3. Provider 管理

**ModelProviderRegistry**:
- 注册/注销 Provider
- 设置默认 Provider
- 按模型名称查找 Provider
- 优先级排序

**ModelRouter**:
- 基于模型名称路由
- 基于任务复杂度路由
- 可自定义复杂度估算函数

### 4. 配置选项（options）

**ChatOptions**:
- 模型名称、温度、最大 Token
- Top-p、Top-k 采样参数
- Stop sequences、Penalty 参数
- 自定义参数映射
- 支持选项合并（merge）

### 5. 异常处理（exception）

**AiServiceException**:
- 错误代码（errorCode）
- 错误上下文（context）
- 统一的异常层次

### 6. 工具类（util & constant）

- **JsonUtils**: JSON 序列化/反序列化（基于 FastJSON2）
- **AiConstants**: 角色、完成原因、Provider ID、错误代码等常量

## 🔧 编译要求

**需要 JDK 17+**

如果系统默认是 JDK 8，请设置 JAVA_HOME：

```powershell
# PowerShell
$env:JAVA_HOME="C:\Program Files\Java\jdk-17"
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"

# 或在 IDE 中配置 JDK 17
```

## 📝 使用示例

### 注册 Provider

```java
ModelProviderRegistry registry = new ModelProviderRegistry();

// 注册 OpenAI Provider
registry.register(new OpenAiProvider(openAiConfig));

// 注册 Ollama Provider
registry.register(new OllamaProvider(ollamaConfig));

// 设置默认 Provider
registry.setDefaultProvider("openai");
```

### 使用 Router

```java
ModelRouter router = new ModelRouter(registry);

// 根据模型名称路由
ModelProvider provider = router.route("gpt-4o", 0.5);

// 根据复杂度路由
double complexity = router.estimateComplexity(userInput);
provider = router.route(null, complexity);
```

### 构建 ChatRequest

```java
ChatRequest request = ChatRequest.builder()
    .messages(List.of(
        ChatMessage.system("你是一个助手"),
        ChatMessage.user("你好")
    ))
    .model("gpt-4o")
    .temperature(0.7)
    .maxTokens(2048)
    .build();
```

## 🎯 设计亮点

### 1. 简洁的 SPI 设计

不同于 Spring AI 的复杂 Options 体系，我们采用：
- 统一的 `ChatModel` 接口
- 简单的 `ChatOptions` POJO
- 基于 Spring Bean 的 Provider 注册（而非 ServiceLoader）

### 2. 智能路由机制

- 支持按模型名称精确匹配
- 支持按任务复杂度动态选择
- 可扩展的复杂度估算策略

### 3. 生产级异常处理

- 统一的错误代码体系
- 丰富的错误上下文信息
- 清晰的异常层次

### 4. 扁平化包结构

按功能域分组（model/spi/options），而非技术分层，便于快速定位代码。

## 🚀 下一步

1. **实现 Provider 适配器**
   - OpenAI Adapter
   - Ollama Adapter
   - Anthropic Adapter（预留）

2. **编写单元测试**
   - Model 类的序列化测试
   - Registry 的并发测试
   - Router 的路由逻辑测试

3. **性能优化**
   - TokenUsage 的对象池
   - Registry 的读写锁优化

## 📊 依赖关系

```
spintale-ai-core (无内部依赖)
    ↑
spintale-ai-client (依赖 core)
spintale-ai-retrieval (依赖 core)
spintale-ai-agent (依赖 core)
...
```

---

**版本**: 3.9.2  
**最后更新**: 2026-05-19
