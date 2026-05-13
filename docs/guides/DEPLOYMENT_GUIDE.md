# SpinTale 部署指南

本指南详细介绍如何在 Linux 虚拟机中部署 SpinTale 项目及其依赖服务（Milvus、Temporal、Redis、PostgreSQL）。

## 一、推荐环境

### 1.1 虚拟机推荐

**推荐方案：Ubuntu 22.04 LTS + 8GB+ RAM + 4 CPU**

- **VirtualBox** (免费，适合开发测试)
  - 官网：https://www.virtualbox.org/
  - 优点：免费、跨平台、易用
  - 缺点：性能略低于 KVM

- **KVM/QEMU** (Linux 原生，性能最佳)
  - 优点：性能最好、资源利用率高
  - 缺点：配置稍复杂

- **VMware Workstation** (商业软件，功能强大)
  - 优点：性能好、功能丰富
  - 缺点：收费

### 1.2 Linux 发行版推荐

**首选：Ubuntu 22.04 LTS**
- 长期支持版本（5 年安全更新）
- Docker 和容器工具支持最好
- 社区活跃，文档丰富
- 软件包更新及时

**备选：**
- Rocky Linux 9 (CentOS 替代品)
- Debian 12 (稳定性极佳)

### 1.3 系统要求

| 组件 | 最低配置 | 推荐配置 |
|------|---------|---------|
| CPU | 2 核 | 4 核+ |
| 内存 | 4GB | 8GB+ |
| 磁盘 | 20GB | 50GB+ SSD |
| 网络 | 10Mbps | 100Mbps+ |

## 二、基础环境安装

### 2.1 更新系统

```bash
sudo apt update && sudo apt upgrade -y
```

### 2.2 安装 Docker

```bash
# 安装 Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# 将当前用户加入 docker 组
sudo usermod -aG docker $USER

# 验证安装
docker --version
docker compose version
```

### 2.3 安装 Docker Compose

Docker Desktop 已包含 Docker Compose v2，无需单独安装。

```bash
# 验证安装
docker compose version
```

### 2.4 安装 Java 17

```bash
# 添加 Adoptium 仓库
wget -qO - https://packages.adoptium.net/artifactory/api/gpg/key/public | sudo apt-key add -
echo "deb https://packages.adoptium.net/artifactory/deb $(lsb_release -cs) main" | sudo tee /etc/apt/sources.list.d/adoptium.list

# 安装 Temurin JDK 17
sudo apt update
sudo apt install temurin-17-jdk -y

# 验证安装
java -version
javac -version
```

### 2.5 安装 Maven

```bash
sudo apt install maven -y

# 验证安装
mvn --version
```

### 2.6 安装 Git

```bash
sudo apt install git -y
git --version
```

## 三、部署依赖服务

### 3.1 克隆项目

```bash
cd ~
git clone <your-repo-url> spintale
cd spintale
```

### 3.2 启动 Milvus + Temporal

```bash
# 确保在项目根目录
cd ~/spintale

# 启动所有服务（Milvus, Temporal, PostgreSQL, Redis）
docker compose up -d

# 查看服务状态
docker compose ps

# 查看日志
docker compose logs -f milvus-standalone
docker compose logs -f temporal
```

### 3.3 验证服务

#### Milvus 验证

```bash
# 检查 Milvus 端口
curl http://localhost:9091/healthz

# 预期输出：{"status": "OK"}
```

#### Temporal 验证

```bash
# 访问 Temporal Web UI
# 浏览器打开：http://localhost:8233

# 或使用 CLI 检查
docker exec temporal tctl --address localhost:7233 namespace describe default
```

#### Redis 验证

```bash
# 如果项目中包含 Redis，检查连接
docker exec redis redis-cli ping
# 预期输出：PONG
```

## 四、配置应用

### 4.1 创建配置文件

```bash
cd ~/spintale/spintale-ai/src/main/resources

# 创建 application-local.yml
cat > application-local.yml << 'EOF'
spring:
  profiles:
    active: local

spintale:
  ai:
    enabled: true
    provider: openai
    model: gpt-3.5-turbo
    
    # OpenAI 配置（替换为你的 API Key）
    openai:
      api-key: sk-your-api-key-here
      baseUrl: https://api.openai.com/v1
    
    # RAG 配置
    rag:
      enabled: true
      vectorStore: milvus
      embeddingModel: bge-small-en-v1.5
      
      milvus:
        uri: http://localhost:19530
        collectionName: spintale_knowledge
    
    # 上下文管理
    context:
      maxMessages: 20
      memoryRetrievalThreshold: 0.6
      longTermMemoryEnabled: true
    
    # 幻觉检测
    hallucinationDetection:
      enabled: true
      threshold: 0.5
      action: WARN

# Redis 配置（用于记忆缓存）
spring.data.redis:
  host: localhost
  port: 6379
  database: 0

# Temporal 配置
temporal:
  test-server:
    enabled: false
  
  workers:
    - task-queue: ai-agent-queue
      name: ai-agent-worker
      workflow-classes:
        - com.spintale.ai.workflow.AgentWorkflowImpl
      activity-beans:
        - retrievalService
        - agentService
  
  connection:
    target: localhost:7233
EOF
```

### 4.2 环境变量配置

```bash
# 创建 .env 文件
cd ~/spintale

cat > .env << 'EOF'
# Docker 卷存储目录
DOCKER_VOLUME_DIRECTORY=./volumes

# OpenAI API Key
OPENAI_API_KEY=sk-your-api-key-here

# Milvus 配置
MILVUS_URI=http://localhost:19530

# Temporal 配置
TEMPORAL_ADDRESS=localhost:7233

# Redis 配置
REDIS_HOST=localhost
REDIS_PORT=6379
EOF
```

## 五、构建与运行

### 5.1 构建项目

```bash
cd ~/spintale

# 清理并编译
mvn clean install -DskipTests

# 仅编译 AI 模块
cd spintale-ai
mvn clean package -DskipTests
```

### 5.2 运行应用

```bash
# 方式 1: 使用 Maven
cd ~/spintale/spintale-ai
mvn spring-boot:run -Dspring.profiles.active=local

# 方式 2: 直接运行 JAR
java -jar target/spintale-ai-3.9.2.jar --spring.profiles.active=local
```

### 5.3 验证应用启动

```bash
# 检查日志
tail -f spintale-ai/logs/application.log

# 访问健康检查端点
curl http://localhost:8080/actuator/health

# 访问 API 文档
# 浏览器打开：http://localhost:8080/swagger-ui.html
```

## 六、初始化向量数据库

### 6.1 创建 Milvus 集合

```bash
# 使用 Python SDK 创建集合（可选）
pip install pymilvus

cat > init_milvus.py << 'EOF'
from pymilvus import connections, FieldSchema, CollectionSchema, DataType, Collection

# 连接 Milvus
connections.connect("default", host="localhost", port="19530")

# 定义集合 schema
fields = [
    FieldSchema(name="id", dtype=DataType.INT64, is_primary=True),
    FieldSchema(name="content", dtype=DataType.VARCHAR, max_length=65535),
    FieldSchema(name="embedding", dtype=DataType.FLOAT_VECTOR, dim=384),
    FieldSchema(name="metadata", dtype=DataType.VARCHAR, max_length=2048)
]

schema = CollectionSchema(fields=fields, description="SpinTale Knowledge Base")

# 创建集合
collection = Collection(name="spintale_knowledge", schema=schema)

# 创建索引
index_params = {
    "index_type": "IVF_FLAT",
    "metric_type": "COSINE",
    "params": {"nlist": 128}
}

collection.create_index(field_name="embedding", index_params=index_params)
print("Milvus collection created successfully!")
EOF

python init_milvus.py
```

### 6.2 导入测试文档

```bash
# 创建测试文档目录
mkdir -p ~/spintale/data/documents

# 添加示例文档
cat > ~/spintale/data/documents/test.md << 'EOF'
# SpinTale 项目文档

SpinTale 是一个基于 AI 的智能对话框架，支持：
- 长期记忆管理
- RAG 检索增强生成
- 幻觉检测
- ReAct 智能体
- MCP 协议支持
EOF

# 使用应用 API 导入文档（启动后）
curl -X POST http://localhost:8080/api/v1/rag/documents \
  -H "Content-Type: multipart/form-data" \
  -F "file=@~/spintale/data/documents/test.md"
```

## 七、生产环境优化

### 7.1 Docker 资源限制

编辑 `docker-compose.yml`，添加资源限制：

```yaml
services:
  milvus-standalone:
    deploy:
      resources:
        limits:
          cpus: '2.0'
          memory: 4G
        reservations:
          cpus: '1.0'
          memory: 2G
  
  temporal:
    deploy:
      resources:
        limits:
          cpus: '1.0'
          memory: 2G
```

### 7.2 数据持久化

确保数据卷正确挂载：

```bash
# 检查卷挂载
docker volume ls
docker inspect milvus-standalone | grep Mounts -A 10

# 备份数据卷
docker run --rm -v spintale_volumes:/data -v $(pwd):/backup alpine \
  tar czf /backup/milvus-backup.tar.gz /data/milvus
```

### 7.3 日志管理

```bash
# 配置 Docker 日志轮转
cat > /etc/docker/daemon.json << 'EOF'
{
  "log-driver": "json-file",
  "log-opts": {
    "max-size": "10m",
    "max-file": "3"
  }
}
EOF

# 重启 Docker
sudo systemctl restart docker
```

### 7.4 监控告警

```bash
# 启用 Prometheus 监控
docker compose -f docker-compose.monitoring.yml up -d

# 访问 Grafana
# 浏览器打开：http://localhost:3000
# 默认账号：admin / admin
```

## 八、常见问题排查

### 8.1 Milvus 启动失败

```bash
# 检查日志
docker compose logs milvus-standalone

# 常见错误：端口占用
sudo netstat -tulpn | grep 19530
sudo lsof -i :19530

# 解决方案：停止占用进程或修改端口
```

### 8.2 Temporal 连接失败

```bash
# 检查 Temporal 状态
docker compose ps temporal

# 检查数据库连接
docker exec temporal-postgresql psql -U temporal -c "SELECT 1"

# 重启 Temporal
docker compose restart temporal
```

### 8.3 内存不足

```bash
# 查看内存使用
free -h
docker stats

# 降低 Milvus 内存配置
# 编辑 docker-compose.yml，添加：
environment:
  - KNOWLEDING_INDEX_MEMORY_SIZE=256MB
```

### 8.4 Java 应用启动失败

```bash
# 检查 Java 版本
java -version

# 检查端口占用
sudo netstat -tulpn | grep 8080

# 查看详细日志
tail -100f spintale-ai/logs/application.log

# 增加 JVM 内存
export JAVA_OPTS="-Xmx2g -Xms1g"
```

## 九、自动化部署脚本

创建一键部署脚本：

```bash
cat > ~/spintale/deploy.sh << 'EOF'
#!/bin/bash
set -e

echo "=== SpinTale 一键部署脚本 ==="

# 1. 检查 Docker
if ! command -v docker &> /dev/null; then
    echo "Docker 未安装，开始安装..."
    curl -fsSL https://get.docker.com -o get-docker.sh
    sudo sh get-docker.sh
fi

# 2. 启动依赖服务
echo "启动 Milvus、Temporal 等服务..."
docker compose up -d

# 3. 等待服务就绪
echo "等待服务启动 (60 秒)..."
sleep 60

# 4. 检查服务状态
docker compose ps

# 5. 构建应用
echo "构建 SpinTale AI 模块..."
cd spintale-ai
mvn clean package -DskipTests

# 6. 启动应用
echo "启动应用..."
nohup java -jar target/spintale-ai-*.jar \
  --spring.profiles.active=local > ../logs/app.log 2>&1 &

echo "=== 部署完成 ==="
echo "应用地址：http://localhost:8080"
echo "Temporal UI: http://localhost:8233"
echo "Milvus 管理：http://localhost:9091"
EOF

chmod +x ~/spintale/deploy.sh
```

## 十、后续步骤

1. **配置 LLM Provider**: 在 `application-local.yml` 中配置你的 OpenAI/Azure/Ollama API Key

2. **导入知识库文档**: 使用 RAG API 上传 PDF/Markdown/Word 文档

3. **测试 Agent 功能**: 调用 `/api/v1/agent/chat` 接口测试智能体

4. **监控与调优**: 通过 Temporal UI 和 Milvus Dashboard 监控系统状态

5. **扩展部署**: 考虑使用 Kubernetes 进行大规模部署

---

**技术支持**: 如有问题，请查看项目文档或提交 Issue。
