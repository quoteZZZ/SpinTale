# SpinTale 文档中心

本目录包含 SpinTale 项目的所有文档，已按类别优化组织。

## 📁 文档结构

```
docs/
├── INDEX.md                 # 本文档索引
├── guides/                  # 开发指南与参考
│   ├── DEVELOPER_GUIDE.md               # 开发者入门指南
│   ├── API_REFERENCE.md                 # API 接口参考文档
│   ├── FRONTEND_INTEGRATION_GUIDE.md    # 前端集成指南
│   ├── DEPLOYMENT_GUIDE.md              # 部署指南 (Milvus + Temporal + Docker)
│   ├── DATABASE_UPGRADE_GUIDE.md        # 数据库升级指南 (新增 AI 表)
│   ├── PROJECT_ANALYSIS_REPORT.md       # 项目架构分析报告
│   ├── SOLUTIONS.md                     # 常见问题解决方案
│   ├── DATABASE_DESIGN.md               # 数据库设计文档
│   ├── TECH_SELECTION_REPORT.md         # 技术选型报告 (Milvus/Pinecone, Temporal/Activiti)
│   ├── JAVA_AI_FRAMEWORK_SELECTION.md   # Java AI 框架选型与 AI 模块结构约束
│   └── FINAL_OPTIMIZATION_REPORT.md     # 最终优化报告 (最新)
└── archive/                 # 归档文档（空）
```

## 📚 文档分类

### 开发指南 (guides/)

| 文档 | 说明 | 适用对象 |
|------|------|----------|
| **DEVELOPER_GUIDE.md** | 开发者入门指南，包含环境配置、构建说明、快速开始 | 新加入开发者 |
| **API_REFERENCE.md** | REST API 接口详细说明，包含请求/响应示例 | 前端/后端开发 |
| **FRONTEND_INTEGRATION_GUIDE.md** | 前端集成详细指南，包含 Vue/React 示例 | 前端开发 |
| **DEPLOYMENT_GUIDE.md** | 完整部署指南，含 Milvus/Temporal Docker 配置、Linux 虚拟机推荐 | DevOps/运维 |
| **DATABASE_UPGRADE_GUIDE.md** | AI 模块数据库升级脚本与详细指南 (10 张新表) ⭐ NEW | 后端开发/DBA |
| **PROJECT_ANALYSIS_REPORT.md** | 完整的项目架构分析报告，含优缺点评估 | 架构师/技术负责人 |
| **SOLUTIONS.md** | 常见问题解决方案和最佳实践手册 | 全体开发人员 |
| **DATABASE_DESIGN.md** | 完整的数据库表结构设计，含 AI 模块 10 张新表 | 后端开发/DBA |
| **TECH_SELECTION_REPORT.md** | 向量数据库和工作流引擎技术选型报告 | 架构师/技术决策者 |
| **JAVA_AI_FRAMEWORK_SELECTION.md** | Java AI 框架优缺点、SpinTale AI 模块结构约束 | 架构师/后端开发 |

## 🔗 快速链接

- [开发者入门](guides/DEVELOPER_GUIDE.md) - 环境配置、构建说明
- [API 参考](guides/API_REFERENCE.md) - REST API 接口文档
- [前端集成](guides/FRONTEND_INTEGRATION_GUIDE.md) - Vue/React 集成示例
- [部署指南](guides/DEPLOYMENT_GUIDE.md) - Milvus + Temporal + Docker 完整部署流程 ⭐ NEW
- [数据库升级](guides/DATABASE_UPGRADE_GUIDE.md) - AI 模块 10 张新表 SQL 脚本与使用指南 ⭐ NEW
- [架构分析](guides/PROJECT_ANALYSIS_REPORT.md) - 系统架构深度分析
- [解决方案](guides/SOLUTIONS.md) - 常见问题与最佳实践
- [数据库设计](guides/DATABASE_DESIGN.md) - 完整的表结构设计和关系图
- [技术选型](guides/TECH_SELECTION_REPORT.md) - Milvus vs Pinecone, Temporal vs Activiti
- [Java AI 框架选型](guides/JAVA_AI_FRAMEWORK_SELECTION.md) - Spring AI、LangChain4j、Semantic Kernel、DJL 等框架对比与结构约束

## 📦 AI 模块核心功能

SpinTale AI 模块提供以下核心能力：

### 1. 智能对话系统
- **长期记忆**: 跨会话记住用户偏好、重要事实
- **幻觉检测**: 实时检测并缓解 AI 幻觉，置信度评分
- **上下文管理**: 智能选择相关上下文，优化 token 使用

### 2. RAG 检索增强生成
- **多格式解析**: PDF、Markdown、Word 文档解析
- **向量检索**: 基于 Milvus 的向量相似度搜索
- **Embedding**: 支持多种 Embedding 模型 (all-MiniLM-L6-v2)

### 3. Agent 系统
- **ReAct Agent**: 完整的 Reasoning + Acting 循环
- **Temporal 编排**: 基于 Temporal 的工作流引擎，支持长流程、自动重试
- **工具调用**: 支持 Weather、HTTP API、文件系统等工具
- **技能管理**: 可扩展的技能注册与执行框架

### 4. MCP 协议支持
- **资源管理**: 标准化资源访问接口
- **工具集成**: Model Context Protocol 工具支持
- **提示词模板**: 可复用的 Prompt 模板系统

### 5. 内容生成
- **多模板支持**: 文章、广告文案、小说等模板
- **流式输出**: 支持 SSE 流式响应
- **多模型适配**: OpenAI、Azure、Ollama、Anthropic

## 🛠️ 技术栈

| 组件 | 技术选型 |
|------|----------|
| 基础框架 | Spring Boot |
| AI 抽象层 | LangChain4j |
| 向量数据库 | Milvus (推荐) / Pinecone (备选) |
| 工作流引擎 | Temporal (推荐) / Activiti (备选) |
| 文档解析 | Apache PDFBox, POI |
| 缓存 | Redis (Redisson) |
| JSON 处理 | FastJSON2 |
| 容错处理 | Resilience4j |
| 监控指标 | Micrometer + Prometheus |

## 📊 项目状态

- **Java 文件数**: 70+ 个
- **核心模块**: 15 个子模块 (新增 workflow 模块)
- **总体评分**: 8.5/10
- **生产就绪**: ✅ 核心功能完善，建议配置 Milvus + Temporal

## 🚀 推荐部署方案

根据技术选型报告，推荐以下生产部署方案：

### 标准方案 (推荐)
- **向量数据库**: Milvus (自部署，数据可控，成本低)
- **工作流引擎**: Temporal (代码即工作流，适合 AI Agent 编排)
- **适用场景**: 企业级私有化部署，数据安全要求高

### 快速启动方案 (备选)
- **向量数据库**: Pinecone (全托管，零运维)
- **工作流引擎**: Temporal
- **适用场景**: 快速原型验证，海外业务，无运维团队

详细对比和成本分析请查看 [技术选型报告](guides/TECH_SELECTION_REPORT.md)。

---

*最后更新：2025-05-13 | 版本：v3.9.2*
