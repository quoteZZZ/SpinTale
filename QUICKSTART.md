# SpinTale AI 快速开始指南

## 🚀 5 分钟快速启动

### 前置要求

- Docker 20.10+ 和 Docker Compose 2.0+
- Java 17+
- Maven 3.8+
- MySQL 8.0+

### 一键部署（推荐）

```bash
# 克隆项目后，在项目根目录执行
./scripts/deploy.sh
```

脚本将自动完成：
1. ✅ 检查 Docker 环境
2. ✅ 启动 Milvus + Temporal 服务
3. ✅ 配置环境变量
4. ✅ 构建应用（可选）

### 手动部署步骤

#### 步骤 1: 启动基础设施

```bash
# 启动 Milvus 向量数据库和 Temporal 工作流引擎
docker compose up -d

# 验证服务状态
curl http://localhost:9091/healthz  # Milvus
curl http://localhost:8233          # Temporal UI
```

#### 步骤 2: 升级数据库

```bash
# 备份现有数据库
mysqldump -u root -p spin_tale > backup_$(date +%Y%m%d).sql

# 执行 AI 模块表结构升级
mysql -u root -p spin_tale < scripts/upgrade_ai_schema.sql

# 验证新表已创建
mysql -u root -p -e "USE spin_tale; SHOW TABLES LIKE 'ai_%';"
```

预期输出 10 张 AI 相关表：
```
ai_conversation
ai_conversation_message
ai_long_term_memory
ai_rag_document
ai_rag_chunk
ai_agent_workflow
ai_skill_registry
ai_hallucination_check
ai_token_usage
ai_mcp_server
```

#### 步骤 3: 配置 API Key

编辑 `spintale-ai/src/main/resources/application-ai.yml`：

```yaml
spintale:
  ai:
    openai:
      api-key: sk-your-actual-api-key-here  # 替换为你的 API Key
```

或使用环境变量：

```bash
export OPENAI_API_KEY=sk-your-actual-api-key-here
```

#### 步骤 4: 启动应用

```bash
cd spintale-ai

# 开发模式运行
mvn spring-boot:run -Dspring-boot.run.profiles=ai

# 或打包后运行
mvn clean package -DskipTests
java -jar target/spintale-ai.jar --spring.profiles.active=ai
```

#### 步骤 5: 测试 API

```bash
# 发送聊天请求
curl -X POST http://localhost:8080/ai/chat/message \
  -H "Content-Type: application/json" \
  -d '{
    "message": "你好，请介绍一下你自己",
    "sessionId": "test-session-001"
  }'
```

预期响应：
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "content": "你好！我是 SpinTale AI 助手...",
    "sessionId": "test-session-001",
    "tokenUsage": {
      "promptTokens": 25,
      "completionTokens": 50,
      "totalTokens": 75
    }
  }
}
```

---

## 📊 访问管理界面

### Temporal UI（工作流监控）

地址：http://localhost:8233

功能：
- 查看工作流执行历史
- 调试 Agent 活动
- 监控任务队列

### Milvus Admin（向量数据库）

地址：http://localhost:9091

功能：
- 查看集合状态
- 监控向量检索性能
- 管理索引

---

## 🔧 常见问题

### Q1: Milvus 启动失败

**症状**: `curl http://localhost:9091/healthz` 无响应

**解决**:
```bash
# 查看日志
docker compose logs milvus-standalone

# 重启服务
docker compose restart milvus-standalone

# 确保端口未被占用
lsof -i :19530
```

### Q2: 数据库升级失败

**症状**: MySQL 报错 `Table already exists`

**解决**:
```bash
# 如果表已存在，跳过升级或手动删除后重试
mysql -u root -p -e "DROP DATABASE IF EXISTS spin_tale_ai_test;"
```

### Q3: API Key 无效

**症状**: 返回 `401 Unauthorized`

**解决**:
```bash
# 检查环境变量
echo $OPENAI_API_KEY

# 或在配置文件中直接设置
vim spintale-ai/src/main/resources/application-ai.yml
```

### Q4: 内存不足

**症状**: OOM 错误

**解决**:
```bash
# 调整 JVM 参数
export JAVA_OPTS="-Xms512m -Xmx2g"

# 或在 Docker Compose 中限制资源
# docker-compose.yml 中添加：
services:
  spintale-ai:
    deploy:
      resources:
        limits:
          memory: 4G
```

---

## 📚 下一步

- 📖 阅读 [开发者指南](docs/guides/DEVELOPER_GUIDE.md)
- 🔌 查看 [API 参考文档](docs/guides/API_REFERENCE.md)
- 🏗️ 了解 [架构设计](docs/guides/DATABASE_DESIGN.md)
- 🚀 查看 [优化报告](docs/guides/FINAL_OPTIMIZATION_REPORT.md)

---

## 🆘 获取帮助

遇到问题？

1. 查看日志：`tail -f spintale-ai/logs/application.log`
2. 检查文档：`docs/guides/`
3. 提交 Issue：GitHub Issues

---

**祝使用愉快！** 🎉
