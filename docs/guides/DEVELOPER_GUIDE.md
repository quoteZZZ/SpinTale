# SpinTale 开发者指南

## 项目概述

SpinTale 是一个基于 Spring Boot 的企业级 AI 后端平台，集成了先进的 AI 能力（长期记忆、幻觉检测、技能系统、MCP 协议）和完整的业务管理功能。

### 核心特性

| 模块 | 评分 | 状态 | 说明 |
|------|------|------|------|
| 长期记忆系统 | 9/10 | ✅ 完善 | 5 种记忆类型、语义检索、重要性评分 |
| 幻觉检测系统 | 8.5/10 | ✅ 完善 | 多维度检测、置信度分级 |
| Skills 技能系统 | 9/10 | ✅ 完善 | 动态技能注册、优先级管理 |
| MCP 协议支持 | 9/10 | ✅ 完善 | Model Context Protocol 服务端 |
| ReAct Agent | 8/10 | ⚠️ 优化中 | 工具调用循环、迭代控制 |
| RAG 检索 | 7/10 | 🔧 增强中 | PDF/Markdown/Word 解析器 |

**总体评分：8.5/10**

---

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.9+
- MySQL 8.x
- Redis 7.x

### 启动步骤

```bash
# 1. 编译项目
mvn -q -DskipTests compile

# 2. 启动服务
mvn -pl spintale-admin -am spring-boot:run
```

### 访问地址

| 服务 | URL |
|------|-----|
| Swagger UI | http://localhost:8080/swagger-ui.html |
| API Docs | http://localhost:8080/v3/api-docs |
| Druid 监控 | http://localhost:8080/druid/ |

---

## 架构设计

### 模块结构

```
spintale-ai/
├── agent/              # AI Agent (ReAct 模式)
├── client/             # AI 客户端适配 (LangChain4j)
├── config/             # Spring 自动配置
├── core/               # 核心接口和服务
├── generation/         # 内容生成服务
├── hallucination/      # 幻觉检测
├── mcp/                # Model Context Protocol
├── memory/             # 短期/长期记忆管理
├── prompt/             # 提示词模板
├── retrieval/          # RAG 检索服务
├── skill/              # 技能管理系统
├── tool/               # 工具定义和执行
└── web/                # REST API 控制器
```

### 技术栈

- **框架**: Spring Boot 4.0.3
- **AI 抽象层**: LangChain4j
- **JSON 处理**: FastJSON2
- **数据库**: MySQL + MyBatis
- **缓存**: Redis
- **文档**: Springdoc OpenAPI

---

## 核心功能详解

### 1. 长期记忆系统

#### 记忆类型

| 类型 | 说明 | 示例 |
|------|------|------|
| FACT | 事实性信息 | "用户喜欢咖啡" |
| EVENT | 事件记录 | "昨天参加了产品会议" |
| PREFERENCE | 偏好设置 | "偏好简洁的回答风格" |
| SUMMARY | 摘要信息 | "过去一周的工作总结" |
| KNOWLEDGE | 知识片段 | "Python 是一种编程语言" |

#### 使用示例

```java
@Autowired
private LongTermMemoryManager memoryManager;

// 添加记忆
LongTermMemory memory = new LongTermMemory();
memory.setUserId("user123");
memory.setType(MemoryType.PREFERENCE);
memory.setContent("用户每天早上 8 点查看新闻");
memoryManager.addMemory(memory);

// 语义搜索
List<LongTermMemory> results = memoryManager.searchMemories(
    "user123", 
    "早晨习惯", 
    5, 
    0.7
);
```

#### 持久化方案

生产环境需配置数据库存储：

```yaml
spintale:
  memory:
    persistence:
      enabled: true
      type: jdbc
    cache:
      enabled: true
      expire-minutes: 30
```

详细实现参考：[SOLUTIONS.md](./SOLUTIONS.md#一记忆系统持久化方案)

---

### 2. 幻觉检测系统

#### 检测策略

| 策略 | 说明 |
|------|------|
| 事实一致性检查 | 对比已知事实库 |
| 可疑模式识别 | 绝对化表述、模糊引用 |
| 内部一致性检查 | 自相矛盾检测 |
| 未验证主张检测 | 无来源统计数据标记 |
| AI 元评估 | 使用 LLM 评估置信度 |

#### 置信度分级

```
≥0.8: 高可信度 → 直接返回
0.5-0.8: 中等可信度 → 添加提示
<0.5: 低可信度 → 警告标记/重新生成
```

#### 使用示例

```java
@Autowired
private HallucinationDetector detector;

HallucinationResult result = detector.detect(
    "根据 2023 年统计，全球有 75% 的人使用 AI 助手",
    context
);

if (result.getConfidenceScore() < 0.5) {
    log.warn("检测到潜在幻觉：{}", result.getSuspiciousPatterns());
}
```

---

### 3. Skills 技能系统

#### 技能定义

```java
AiSkill skill = AiSkill.builder()
    .name("data_analysis")
    .description("数据分析技能")
    .priority(10)
    .enabled(true)
    .toolNames(List.of("sql_query", "chart_generator"))
    .build();

skillManager.registerSkill(skill);
```

#### 技能激活

```java
// 为用户激活技能
skillManager.activateSkillForUser("user123", "data_analysis");

// 获取用户可用技能
List<AiSkill> skills = skillManager.getUserSkills("user123");
```

---

### 4. MCP 协议支持

#### 服务端配置

```java
@Configuration
public class McpConfig {
    
    @Bean
    public McpServer mcpServer(AiToolProvider toolProvider) {
        return McpServer.builder()
            .name("spintale-mcp")
            .version("1.0.0")
            .tools(toolProvider.getTools())
            .build();
    }
}
```

#### 工具暴露

```java
@RestController
@RequestMapping("/api/mcp")
public class McpController {
    
    @PostMapping("/tools")
    public ResponseEntity<List<McpTool>> listTools() {
        return ResponseEntity.ok(toolProvider.getTools());
    }
}
```

---

### 5. RAG 检索增强

#### 支持的文档格式

| 格式 | 解析器 | 依赖 |
|------|--------|------|
| PDF | PdfDocumentParser | Apache PDFBox |
| Markdown | MarkdownDocumentParser | commonmark-java |
| Word (.docx) | WordDocumentParser | Apache POI |

#### 文档索引

```java
@Autowired
private DocumentParserFactory parserFactory;

@Autowired
private EmbeddingRetrievalService retrievalService;

// 解析并索引文档
File pdfFile = new File("docs/guide.pdf");
List<Document> docs = parserFactory.parseFile(pdfFile, Map.of("category", "manual"));
retrievalService.indexDocuments(docs);
```

#### 混合检索

```yaml
spintale:
  rag:
    retrieval:
      hybrid-search-enabled: true  # 语义 + 关键词
      min-similarity-score: 0.7
      default-max-results: 5
```

详细实现参考：[SOLUTIONS.md](./SOLUTIONS.md#二 rag 文档解析器)

---

### 6. ReAct Agent

#### 基本使用

```java
@Autowired
private ReActAgent agent;

AgentResult result = agent.execute(
    "查询北京今天的天气，并推荐适合的户外活动"
);

if (result.isSuccess()) {
    System.out.println(result.getContent());
    System.out.println("使用的工具：" + result.getExecutedTools());
}
```

#### 自定义工具

```java
Map<String, Function<Map<String, Object>, String>> tools = Map.of(
    "weather_query", args -> {
        String city = (String) args.get("city");
        return weatherService.query(city);
    },
    "activity_recommend", args -> {
        String weather = (String) args.get("weather");
        return activityService.recommend(weather);
    }
);

ReActAgent customAgent = new ReActAgent(chatModel, tools, toolSpecs);
```

---

## 最佳实践

### 1. 记忆管理

- ✅ 定期清理过期记忆（建议 90 天）
- ✅ 对高频访问的记忆启用 Redis 缓存
- ✅ 使用记忆压缩减少存储占用
- ❌ 避免存储敏感个人信息

### 2. 幻觉防控

- ✅ 对关键事实进行多重验证
- ✅ 设置合理的置信度阈值
- ✅ 记录幻觉检测结果用于模型优化
- ❌ 不要完全信任 AI 生成的统计数据

### 3. RAG 优化

- ✅ 文档分块大小控制在 500-1000 字符
- ✅ 为文档添加丰富的元数据
- ✅ 定期更新向量索引
- ❌ 避免索引过大的单一文档

### 4. 性能调优

```yaml
# 推荐的缓存配置
spintale:
  memory:
    cache:
      max-size: 10000
      expire-minutes: 30
  
  rag:
    chunking:
      max-segment-size: 800
      max-overlap-size: 120
```

---

## 常见问题

### Q1: 记忆重启后丢失？

**A**: 默认使用内存存储，生产环境需配置 JDBC 或 Redis 持久化。参考 [SOLUTIONS.md](./SOLUTIONS.md)。

### Q2: RAG 检索精度低？

**A**: 调整以下参数：
- 降低 `min-similarity-score` (默认 0.7)
- 增加 `default-max-results` (默认 5)
- 优化文档分块策略

### Q3: Agent 陷入无限循环？

**A**: 设置合理的 `maxIterations` (默认 10)，并检查工具返回结果格式。

### Q4: MCP 工具无法调用？

**A**: 确认：
- 工具已正确注册到 `AiToolProvider`
- MCP 服务器已启动
- 工具参数格式符合 JSON Schema

---

## 开发指南

### 添加新工具

1. 实现 `AiTool` 接口
2. 在 `application.yml` 中配置工具元数据
3. 通过 `AiToolProvider` 注册工具
4. 编写单元测试

### 扩展记忆存储

1. 实现 `MemoryStore` 接口
2. 创建 Spring Bean
3. 配置 `spintale.memory.persistence.type`

### 自定义检测策略

1. 扩展 `HallucinationDetector` 类
2. 实现新的检测逻辑
3. 在配置中启用策略

---

## 文档导航

| 文档 | 用途 |
|------|------|
| [README.md](./README.md) | 项目总览和快速开始 |
| [SOLUTIONS.md](./SOLUTIONS.md) | 关键问题解决方案 |
| [API_DOCUMENTATION.md](./spintale-ai/API_DOCUMENTATION.md) | API 参考文档 |
| [FRONTEND_INTEGRATION_GUIDE.md](./spintale-ai/FRONTEND_INTEGRATION_GUIDE.md) | 前端集成指南 |
| [PROJECT_ANALYSIS_REPORT.md](./PROJECT_ANALYSIS_REPORT.md) | 完整项目分析报告 |

---

## 归档文档

历史文档已移至 `docs/archive/` 目录：

- `ANALYSIS_REPORT.md` - 初始分析报告
- `OPTIMIZATION_PLAN.md` - 优化计划
- `UPGRADE_MEMORY_HALLUCINATION.md` - 记忆和幻觉检测升级
- `UPGRADE_SKILLS_MCP.md` - 技能和 MCP 升级

---

## 贡献指南

1. Fork 项目仓库
2. 创建特性分支 (`git checkout -b feature/amazing-feature`)
3. 提交变更 (`git commit -m 'Add amazing feature'`)
4. 推送到分支 (`git push origin feature/amazing-feature`)
5. 提交 Pull Request

---

## 许可证

本项目采用 MIT 许可证。详见 LICENSE 文件。

---

**最后更新**: 2025-01-XX  
**维护团队**: SpinTale Development Team
