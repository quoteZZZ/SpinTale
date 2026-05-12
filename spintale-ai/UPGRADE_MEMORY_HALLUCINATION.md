# AI 模块升级说明 - 长期记忆与幻觉检测

## 升级概述

本次升级重点解决了 AI 系统的两个核心问题：
1. **长时间记忆上下文** - 实现跨会话的记忆能力，让 AI 能够记住用户的重要信息和偏好
2. **幻觉问题** - 实时检测和缓解 AI 产生的不准确、编造或矛盾的信息

## 新增功能模块

### 1. 长期记忆系统 (Long-term Memory)

#### 核心组件

| 文件 | 类型 | 功能描述 |
|------|------|----------|
| `LongTermMemory.java` | 实体类 | 长期记忆数据模型，支持多种记忆类型（事实、事件、偏好、摘要） |
| `LongTermMemoryManager.java` | 接口 | 记忆管理接口，定义存储、检索、更新、删除等操作 |
| `InMemoryLongTermMemoryManager.java` | 服务实现 | 基于内存和向量嵌入的记忆管理器，支持语义搜索和重要性评分 |

#### 主要特性

- **记忆类型分类**: 
  - FACT (事实): 用户陈述的事实信息
  - EVENT (事件): 重要事件记录
  - PREFERENCE (偏好): 用户喜好和习惯
  - SUMMARY (摘要): 多段记忆的压缩摘要
  - KNOWLEDGE (知识): 知识库内容

- **重要性评分机制**: 
  - 自动计算初始重要性 (0.0-1.0)
  - 根据访问频率动态提升评分
  - 支持手动调整

- **语义检索**: 
  - 基于向量相似度搜索相关记忆
  - 可配置相似度阈值
  - 按重要性和相关性排序

- **记忆压缩**: 
  - 当记忆数量超过阈值时自动触发
  - 将多个相关记忆合并为摘要
  - 减少上下文长度，提高效率

- **过期清理**: 
  - 支持设置记忆过期时间
  - 定期清理过期和低价值记忆

### 2. 幻觉检测系统 (Hallucination Detection)

#### 核心组件

| 文件 | 类型 | 功能描述 |
|------|------|----------|
| `HallucinationDetectionService.java` | 服务类 | 综合幻觉检测服务，包含多种检测策略 |

#### 检测策略

1. **事实一致性检查**
   - 对比已知事实库
   - 检测明显矛盾的表述
   - 支持用户自定义事实

2. **可疑模式识别**
   - 绝对化表述检测 ("绝对"、"肯定"、"百分之百")
   - 模糊权威引用 ("研究表明"但无具体来源)
   - 时间矛盾检测
   - 过于具体的数字 (可能是编造的)

3. **内部一致性检查**
   - 检测自相矛盾的表述
   - 异常时间跨度识别
   - 逻辑冲突分析

4. **未经验证主张检测**
   - 识别没有来源的统计数据
   - 标记缺乏依据的断言

5. **AI 元评估**
   - 使用 AI 模型评估自身回复的可信度
   - 生成置信度评分 (0.0-1.0)
   - 提供改进建议

#### 响应处理

- **置信度分级**:
  - ≥0.8: 高可信度，直接返回
  - 0.5-0.8: 中等可信度，添加提示信息
  - <0.5: 低可信度，添加警告标记

- **处理方式配置**:
  - WARN: 仅添加警告提示
  - REGENERATE: 尝试重新生成
  - BLOCK: 阻止低可信度回复

### 3. 增强型聊天服务 (Enhanced AiChatService)

#### 核心组件

| 文件 | 类型 | 功能描述 |
|------|------|----------|
| `EnhancedAiChatService.java` | 服务类 | 集成记忆和幻觉检测的增强聊天服务 |

#### 工作流程

```
用户请求
    ↓
1. 获取/创建会话
    ↓
2. 检索长期记忆 (语义搜索)
    ↓
3. 增强系统提示 (注入记忆)
    ↓
4. 构建优化上下文 (智能选择历史)
    ↓
5. 调用底层 AI 服务生成回复
    ↓
6. 幻觉检测
    ├─ 检测到幻觉 → 添加警告/重新生成
    └─ 正常 → 继续
    ↓
7. 保存对话到短期记忆
    ↓
8. 提取重要信息保存到长期记忆
    ↓
返回给用户
```

#### 关键优化

- **上下文管理**: 
  - 限制最大上下文消息数 (默认 20 条)
  - 优先保留最近和最相关的消息
  - 避免 token 超限

- **记忆注入**: 
  - 动态将相关记忆添加到系统提示
  - 格式化为易读的背景信息
  - 提高回复的个性化程度

- **自动记忆提取**: 
  - 识别用户偏好陈述 ("我喜欢...")
  - 捕捉事实性信息 ("我是...", "我有...")
  - 记录重要事件

### 4. 自动配置 (Auto Configuration)

#### 配置类

| 文件 | 功能 |
|------|------|
| `AiEnhancedAutoConfig.java` | 增强功能自动配置 |
| `AiProperties.java` (扩展) | 新增上下文和幻觉检测配置 |

#### 配置参数

```yaml
spintale:
  ai:
    # 上下文管理配置
    context:
      maxMessages: 20                    # 最大上下文消息数
      memoryRetrievalThreshold: 0.6      # 记忆检索相似度阈值
      longTermMemoryEnabled: true        # 是否启用长期记忆
    
    # 幻觉检测配置
    hallucinationDetection:
      enabled: true                      # 是否启用幻觉检测
      threshold: 0.5                     # 幻觉判定阈值
      action: WARN                       # 处理方式：WARN/REGENERATE/BLOCK
```

## 技术架构

### 依赖关系

```
EnhancedAiChatService
├── AiChatService (delegate)
├── ConversationManager
├── LongTermMemoryManager
│   ├── EmbeddingStore<LongTermMemory>
│   ├── EmbeddingModel
│   └── ChatModel (用于记忆压缩)
└── HallucinationDetectionService
    └── ChatModel (用于元评估)
```

### 数据存储

当前实现使用内存存储：
- `ConcurrentHashMap` 存储记忆
- `InMemoryEmbeddingStore` 存储向量嵌入

**生产环境建议**:
- 使用关系数据库 (MySQL/PostgreSQL) 存储记忆元数据
- 使用向量数据库 (Milvus/Pinecone/Weaviate) 存储嵌入向量
- 使用 Redis 缓存热点记忆

## 使用示例

### 1. 基础使用 (自动生效)

```java
@Autowired
private AiChatService aiChatService;

// 增强服务会自动处理记忆和幻觉检测
ChatRequest request = ChatRequest.builder()
    .sessionId("session-123")
    .message("我记得上次说过我喜欢吃川菜")
    .build();

ChatResponse response = aiChatService.chat(request);
// AI 会记住用户的偏好并在后续对话中使用
```

### 2. 手动管理长期记忆

```java
@Autowired
private LongTermMemoryManager memoryManager;

// 添加记忆
LongTermMemory memory = new LongTermMemory();
memory.setUserId("user-123");
memory.setType(LongTermMemory.MemoryType.PREFERENCE);
memory.setContent("用户喜欢喝不加糖的咖啡");
memory.setImportanceScore(0.8);
memoryManager.addMemory(memory);

// 检索记忆
List<LongTermMemory> memories = memoryManager.searchMemories(
    "user-123", "咖啡偏好", 5, 0.6);

// 更新记忆
memoryManager.updateImportanceScore(memory.getId(), 0.9);
```

### 3. 配置幻觉检测

```java
@Configuration
public class AiConfig {
    
    @Bean
    public AiChatService enhancedAiChatService(
            AiChatService delegate,
            ConversationManager conversationManager,
            LongTermMemoryManager longTermMemoryManager,
            HallucinationDetectionService hallucinationDetectionService,
            ChatModel chatModel,
            AiProperties properties) {
        
        EnhancedAiChatService service = new EnhancedAiChatService(
            delegate, conversationManager, longTermMemoryManager,
            hallucinationDetectionService, chatModel);
        
        // 禁用幻觉检测 (适用于创意写作场景)
        service.setHallucinationDetectionEnabled(false);
        
        return service;
    }
}
```

## 性能考虑

### Token 优化

- 上下文消息限制防止 token 爆炸
- 记忆压缩减少历史长度
- 只检索最相关的记忆 (top-k)

### 延迟控制

- 记忆检索并行执行
- 幻觉检测可配置为异步
- 向量搜索使用近似最近邻 (ANN)

### 内存管理

- 定期清理过期记忆
- LRU 缓存常用记忆
- 分页加载大量记忆

## 未来改进方向

1. **AI 驱动的记忆提取**
   - 使用 LLM 自动从对话中提取重要信息
   - 更准确的事实/偏好识别

2. **多模态记忆**
   - 支持图片、音频等多媒体记忆
   - 跨模态检索

3. **分布式记忆存储**
   - 支持集群部署
   - 记忆分片和复制

4. **高级幻觉检测**
   - 外部知识源验证
   - 实时事实核查 API 集成
   - 多模型交叉验证

5. **可解释性**
   - 记忆使用追溯
   - 幻觉检测原因可视化
   - 置信度来源解释

## 测试建议

```java
@SpringBootTest
public class EnhancedAiChatServiceTest {
    
    @Autowired
    private AiChatService aiChatService;
    
    @Autowired
    private LongTermMemoryManager memoryManager;
    
    @Test
    public void testLongTermMemory() {
        // 第一轮对话 - 建立记忆
        ChatRequest req1 = ChatRequest.builder()
            .sessionId("test-session")
            .message("我是一名软件工程师，喜欢编程")
            .build();
        aiChatService.chat(req1);
        
        // 第二轮对话 - 验证记忆
        ChatRequest req2 = ChatRequest.builder()
            .sessionId("test-session")
            .message("我的职业是什么？")
            .build();
        ChatResponse resp = aiChatService.chat(req2);
        
        assertTrue(resp.getContent().contains("软件工程师"));
    }
    
    @Test
    public void testHallucinationDetection() {
        // 测试幻觉检测
        String falseClaim = "根据 2023 年哈佛大学的研究，每天喝咖啡可以延长寿命 20 年";
        
        HallucinationDetectionService detector = 
            new HallucinationDetectionService(chatModel);
        HallucinationResult result = detector.detectHallucination(
            "user-123", "", falseClaim);
        
        assertTrue(result.getIsHallucination());
        assertTrue(result.getOverallConfidence() < 0.5);
    }
}
```

## 总结

本次升级为 SpinTale AI 模块带来了企业级的长期记忆和幻觉检测能力：

✅ **长期记忆**: 跨会话记住用户信息，提供个性化体验  
✅ **幻觉检测**: 实时识别和缓解不准确信息，提高可靠性  
✅ **上下文优化**: 智能管理对话历史，平衡效果和成本  
✅ **可配置性**: 灵活的参数配置适应不同场景  
✅ **可扩展性**: 模块化设计便于后续功能扩展

这些改进使得 AI 系统更加智能、可靠，适合企业级应用场景。
