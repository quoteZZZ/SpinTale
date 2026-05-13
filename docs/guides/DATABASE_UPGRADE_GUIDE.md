# SpinTale AI 模块数据库升级指南

## 📋 概述

本指南用于将现有 SpinTale 数据库升级以支持完整的 AI 功能模块，包括：
- **对话管理**：会话和消息持久化
- **长期记忆系统**：用户记忆存储与检索
- **RAG 知识库**：文档解析与向量检索
- **Agent 工作流**：Temporal 工作流引擎集成
- **质量监控**：幻觉检测与 Token 统计
- **MCP 支持**：外部工具服务器管理

## 🎯 新增数据表清单

| 表名 | 用途 | 关键字段 |
|------|------|----------|
| `ai_conversation` | AI 会话管理 | session_id, user_id, model_name, mode |
| `ai_conversation_message` | 消息记录 | role, content, token_count, tool_calls |
| `ai_long_term_memory` | 长期记忆 | memory_type, importance_score, vector_id |
| `ai_rag_document` | RAG 文档元数据 | file_type, content_hash, chunk_count |
| `ai_rag_chunk` | 文档分片 | chunk_index, vector_id, keywords |
| `ai_agent_workflow` | Agent 工作流 | workflow_id, current_state, output_data |
| `ai_skill_registry` | 技能注册中心 | skill_code, config_schema, is_enabled |
| `ai_hallucination_check` | 幻觉检测 | hallucination_score, risk_level, evidence |
| `ai_token_usage` | Token 统计 | stat_date, prompt_tokens, estimated_cost |
| `ai_mcp_server` | MCP 服务器配置 | transport_type, capabilities, status |

## 🚀 快速开始

### 前置条件

1. **MySQL 版本**: 8.0+ 或 MariaDB 10.5+
2. **字符集**: utf8mb4
3. **权限**: 需要 CREATE TABLE, INSERT 权限
4. **备份**: 建议先备份现有数据库

### 步骤 1: 备份数据库

```bash
# 备份整个数据库
mysqldump -u root -p spin_tale > spin_tale_backup_$(date +%Y%m%d).sql

# 或仅备份现有表结构
mysqldump -u root -p --no-data spin_tale > spin_tale_schema_backup.sql
```

### 步骤 2: 执行升级脚本

```bash
# 方式一：命令行直接执行
mysql -u root -p spin_tale < scripts/upgrade_ai_schema.sql

# 方式二：进入 MySQL 客户端执行
mysql -u root -p
USE spin_tale;
SOURCE scripts/upgrade_ai_schema.sql;
```

### 步骤 3: 验证安装

```bash
# 检查新表是否创建成功
mysql -u root -p spin_tale -e "SHOW TABLES LIKE 'ai_%';"

# 预期输出 10 张表:
# ai_conversation
# ai_conversation_message
# ai_long_term_memory
# ai_rag_document
# ai_rag_chunk
# ai_agent_workflow
# ai_skill_registry
# ai_hallucination_check
# ai_token_usage
# ai_mcp_server
```

### 步骤 4: 验证初始数据

```sql
-- 检查技能注册表
SELECT skill_code, skill_name, is_enabled FROM ai_skill_registry;

-- 检查 MCP 服务器配置
SELECT server_name, transport_type, status FROM ai_mcp_server;
```

## 🔧 字段设计说明

### 核心设计原则

1. **混合存储架构**: 
   - MySQL: 结构化数据、元数据、关系数据
   - Redis: 热点缓存、会话状态
   - Milvus: 向量 embeddings
   - Temporal: 工作流状态

2. **JSON 扩展字段**: 所有表都包含 `metadata` JSON 字段，支持灵活扩展

3. **索引优化**: 
   - 唯一索引确保数据一致性
   - 复合索引优化查询性能
   - 降序索引支持重要性排序

4. **软删除支持**: 使用 `status` 字段而非物理删除

### 关键字段详解

#### ai_conversation
- `session_id`: UUID 格式，前端传递，支持无状态会话
- `mode`: 区分 chat/agent/rag 模式，用于路由不同处理逻辑
- `metadata`: 存储 { "tags": ["工作", "技术"], "theme": "AI 开发" }

#### ai_long_term_memory
- `memory_type`: 
  - episodic: 事件记忆 (如"昨天用户提到喜欢咖啡")
  - semantic: 知识记忆 (如"用户是 Java 开发者")
  - procedural: 技能记忆 (如"用户偏好使用 Maven")
- `importance_score`: 0.0-1.0，用于记忆淘汰策略
- `vector_id`: 关联 Milvus 中的向量 ID

#### ai_rag_chunk
- `chunk_index`: 保证文档片段顺序
- `vector_id`: Milvus 向量 ID，用于相似度搜索
- `keywords`: 提取的关键词数组，用于混合检索

## 📊 数据库关系图

```
┌─────────────────────┐       ┌──────────────────────┐
│  sys_user (现有)    │       │  ai_conversation     │
│  - id               │◄──────│  - user_id           │
│  - username         │       │  - session_id        │
└─────────────────────┘       └──────────┬───────────┘
                                         │
                                         │ session_id
                                         ▼
                               ┌──────────────────────┐
                               │ ai_conversation_msg  │
                               │ - session_id         │
                               │ - role               │
                               │ - content            │
                               └──────────┬───────────┘
                                          │
                                          │ message_id
                                          ▼
                               ┌──────────────────────┐
                               │ ai_hallucination_chk │
                               │ - message_id         │
                               │ - risk_level         │
                               └──────────────────────┘

┌─────────────────────┐       ┌──────────────────────┐
│  ai_rag_document    │──────►│  ai_rag_chunk        │
│  - doc_id           │       │  - doc_id            │
│  - file_path        │       │  - vector_id ────────┼──► Milvus
│  - content_hash     │       │  - chunk_index       │
└─────────────────────┘       └──────────────────────┘

┌─────────────────────┐       ┌──────────────────────┐
│ ai_long_term_memory │       │  ai_agent_workflow   │
│ - vector_id ────────┼──► Milvus                    │
│ - user_id           │       │  - workflow_id ──────┼──► Temporal
└─────────────────────┘       └──────────────────────┘
```

## 🔍 常见问题排查

### Q1: 表创建失败，提示"Table already exists"
**解决**: 脚本使用 `CREATE TABLE IF NOT EXISTS`，如果已存在可跳过。如需重置：
```sql
DROP TABLE IF EXISTS ai_conversation_message;
DROP TABLE IF EXISTS ai_conversation;
-- ... 依次删除其他表
```

### Q2: 字符集错误
**解决**: 确保 MySQL 配置正确：
```sql
ALTER DATABASE spin_tale CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### Q3: JSON 字段不支持
**解决**: MySQL < 5.7.8 不支持 JSON 类型，需升级到 8.0+ 或使用 TEXT 替代。

### Q4: 外键约束冲突
**解决**: 脚本已设置 `SET FOREIGN_KEY_CHECKS = 0`，如仍有问题检查 sys_user 表是否存在。

## 📈 性能优化建议

### 1. 索引优化
```sql
-- 为高频查询添加覆盖索引
ALTER TABLE ai_conversation_message 
ADD INDEX idx_session_role_created (session_id, role, created_at);

-- 为记忆检索添加复合索引
ALTER TABLE ai_long_term_memory
ADD INDEX idx_user_importance (user_id, importance_score DESC);
```

### 2. 分区策略 (大数据量时)
```sql
-- 按月分区消息表
ALTER TABLE ai_conversation_message
PARTITION BY RANGE (YEAR(created_at) * 100 + MONTH(created_at)) (
    PARTITION p202401 VALUES LESS THAN (202402),
    PARTITION p202402 VALUES LESS THAN (202403),
    -- ...
    PARTITION p_max VALUES LESS THAN MAXVALUE
);
```

### 3. 归档策略
```sql
-- 创建归档表
CREATE TABLE ai_conversation_message_archive LIKE ai_conversation_message;

-- 迁移 90 天前数据
INSERT INTO ai_conversation_message_archive
SELECT * FROM ai_conversation_message 
WHERE created_at < DATE_SUB(NOW(), INTERVAL 90 DAY);

DELETE FROM ai_conversation_message 
WHERE created_at < DATE_SUB(NOW(), INTERVAL 90 DAY);
```

## 🔐 安全建议

1. **敏感数据加密**: 对 `content` 字段应用 AES 加密
2. **访问控制**: 限制数据库用户权限，仅授予必要操作
3. **审计日志**: 开启 MySQL 慢查询日志和通用日志
4. **备份策略**: 每日全量备份 + binlog 增量备份

## 📝 下一步

1. **配置应用**: 更新 `application-local.yml` 中的数据库连接
2. **启动服务**: 运行 Docker Compose 启动 Milvus 和 Temporal
3. **测试验证**: 运行集成测试验证所有功能
4. **监控告警**: 配置 Prometheus + Grafana 监控数据库性能

---

**文档版本**: v1.0  
**最后更新**: 2024  
**维护者**: SpinTale Team
