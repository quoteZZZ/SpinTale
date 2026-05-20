# AI 模块结构调整建议

## 结论

建议拆掉原来的 `spintale-ai` 聚合壳目录，将 AI 子模块直接放在根工程同一层：

```text
SpinTale
├── spintale-admin
├── spintale-common
├── spintale-framework
├── spintale-system
├── spintale-ai-core
├── spintale-ai-api
├── spintale-ai-agent
├── spintale-ai-retrieval
├── spintale-ai-providers
└── spintale-ai-starter
```

这样做的核心原因是：当前 AI 模块已经不是一个可以独立发布的二级产品，而是主工程的一组能力模块。继续保留 `spintale-ai` 二级聚合会让 Maven reactor、IDE 模块、包边界和 RuoYi 集成关系都变得更绕。

## 推荐依赖方向

```text
spintale-admin
└── spintale-ai-starter
    ├── spintale-ai-providers
    ├── spintale-ai-agent
    ├── spintale-ai-retrieval
    ├── spintale-ai-api
    └── spintale-ai-core
```

模块职责：

| 模块 | 职责 |
| --- | --- |
| `spintale-ai-core` | 核心模型、SPI、异常、指标、注解，不依赖 Spring Boot starter 和 RuoYi |
| `spintale-ai-api` | 面向业务的 facade、advisor 基础链路、prompt API |
| `spintale-ai-agent` | Agent、工具、记忆、工作流 |
| `spintale-ai-retrieval` | RAG、文档解析、向量检索 |
| `spintale-ai-providers` | LangChain4j、OpenAI、Ollama、本地模型路由适配 |
| `spintale-ai-starter` | Spring Boot 自动配置、属性绑定、Web 暴露、starter 装配 |
| `spintale-admin` | RuoYi 管理端入口，只依赖 `spintale-ai-starter` |

## 边界规则

1. AI 基础模块不能反向依赖 `spintale-ai-starter`。
2. AI starter 不直接依赖 RuoYi 的 `AjaxResult`、`SecurityUtils`、`@Log`、权限表达式。
3. RuoYi 权限、审计、菜单、控制台页面应放在 `spintale-admin`，或后续单独新增 `spintale-ai-ruoyi-adapter`。
4. Provider 实现只能放在 `spintale-ai-providers`，不要放进 `spintale-ai-api`。
5. RAG advisor 放在 `spintale-ai-retrieval`，Memory advisor 放在 `spintale-ai-agent`，避免 API 模块持有具体能力实现。

## 本次已执行的调整

1. 拆除 `spintale-ai` 二级聚合模块，AI 子模块提升为根工程同层模块。
2. 根 `pom.xml` 直接声明 AI 子模块，`spintale-admin` 改为只依赖 `spintale-ai-starter`。
3. 修正 AI 模块间循环依赖，`api` 不再依赖 `retrieval`。
4. 将 `MemoryAdvisor` 下沉到 `spintale-ai-agent`，将 `RagAdvisor` 下沉到 `spintale-ai-retrieval`。
5. 删除 API 层中错误放置的 provider 实现。
6. 将 starter 的 RAG Web 和异常处理改为 RuoYi 无关实现。
7. 修复多处旧 API、旧属性模型、编码损坏导致的编译问题。

## 后续优化建议

1. 新增 `spintale-ai-ruoyi-adapter`：集中承接 RuoYi 权限、审计日志、菜单接口和 `AjaxResult` 适配。
2. 重建 Milvus 实现：当前 Milvus 向量库先保留为编译安全的占位实现，需要按实际 SDK 版本重新实现连接、collection 管理和向量 CRUD。
3. 清理编码损坏文档和注释：现有中文注释大量 mojibake，建议统一转换为 UTF-8 后再维护。
4. 为 AI 模块补最小集成测试：至少覆盖 provider 路由、advisor 链、RAG 文档索引、starter 自动配置加载。
5. 收敛属性模型：`AiProperties` 目前承载过重，建议按 provider、rag、memory、agent、telemetry 分组并保持命名一致。
