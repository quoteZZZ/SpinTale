# SpinTale Backend

SpinTale Backend 是 SpinTale 项目的后端服务，基于 Spring Boot 4、Spring Security、MyBatis、Druid、Redis、JWT、Quartz 和 Springdoc 构建。当前仓库只维护后端代码，前端项目独立放在 `D:\GitCode\SpinTale1.0\SpinTale-Vue`。

## 当前状态

| 项目 | 说明 |
| --- | --- |
| 后端仓库 | `D:\GitCode\SpinTale1.0\SpinTale` |
| 前端仓库 | `D:\GitCode\SpinTale1.0\SpinTale-Vue` |
| Maven 坐标 | `com.spintale:spintale:3.9.2` |
| 启动模块 | `spintale-admin` |
| 启动类 | `com.spintale.SpinTaleApplication` |
| 默认端口 | `8080` |
| 接口根路径 | `/` |
| 数据库 | MySQL `SpinTale` |
| Redis | `localhost:6379` |

## 技术栈

| 组件 | 当前版本或用途 |
| --- | --- |
| JDK | 17 |
| Spring Boot | 4.0.3 |
| MyBatis Spring Boot | 4.0.1 |
| Druid | 1.2.28 |
| PageHelper | 2.1.1 |
| Fastjson2 | 2.0.61 |
| Springdoc OpenAPI | 3.0.2 |
| MySQL | 8.x |
| Redis | 7.x |
| Maven | 3.9.x |

## 模块结构

```text
SpinTale
├─ spintale-admin      # Web 启动入口、系统接口、监控接口、公共接口
├─ spintale-common     # 通用工具、常量、注解、基础响应、Redis 工具
├─ spintale-framework  # 安全认证、Web 配置、数据源、过滤器、异常处理
├─ spintale-system     # 用户、角色、菜单、部门、岗位、字典、参数、公告
├─ spintale-quartz     # 定时任务和任务日志
├─ spintale-generator  # 代码生成器
├─ spintale-ai         # AI 模块（LangChain4j、RAG、Agent、Temporal 工作流）
├─ sql                 # 初始化 SQL
├─ bin                 # Windows 脚本
└─ doc                 # 项目文档资料
```

### AI 模块结构 (spintale-ai)

```text
spintale-ai
├─ core            # 框架无关核心模型、注解、异常、Prompt、服务接口
├─ capability      # Advisor、Memory、Cache、Hallucination、Observability
├─ agent           # ReAct Agent、多 Agent、Temporal 工作流编排
├─ generation      # 内容生成（模板、服务）
├─ retrieval       # RAG 检索（Milvus、文档解析、Graph RAG）
├─ tool            # ToolRegistry、MCP、内置工具
├─ infrastructure  # 外部模型 provider、Spring 配置、客户端、代理、适配器
├─ support         # 日志脱敏等支撑能力
└─ web             # Web 控制器、DTO、异常处理
```

AI 框架选型与包结构约束见 [Java AI 框架选型与 SpinTale 结构优化](docs/guides/JAVA_AI_FRAMEWORK_SELECTION.md)。

## 本地环境

当前推荐的本机安装路径如下，JDK 使用本机已安装的 Microsoft OpenJDK 17：

| 软件 | 路径 |
| --- | --- |
| IntelliJ IDEA | `D:\JetBrains\IntelliJ IDEA 2026.1.1` |
| JDK | `C:\Users\v1589\.jdks\ms-17.0.19` |
| Maven | `D:\Maven` |
| MySQL | `D:\MySQL` |
| Redis | `D:\Redis` |
| Navicat | `D:\Navicat16` |
| Tiny RDM | `D:\TinyRDM` |

PowerShell 临时环境变量示例：

```powershell
$env:JAVA_HOME='C:\Users\v1589\.jdks\ms-17.0.19'
$env:Path='C:\Users\v1589\.jdks\ms-17.0.19\bin;D:\Maven\bin;D:\MySQL\bin;D:\Redis;' + $env:Path
```

## 数据库和缓存

后端默认读取 `spintale-admin/src/main/resources/application.yml` 和 `application-druid.yml`。

| 配置项 | 默认值 |
| --- | --- |
| MySQL 地址 | `localhost:3306` |
| MySQL 数据库 | `SpinTale` |
| MySQL 用户 | `root` |
| MySQL 密码 | `365061` |
| Redis 地址 | `localhost:6379` |
| Redis 数据库 | `0` |
| Redis 密码 | `365061` |
| 文件上传目录 | `D:/SpinTale/uploadPath` |
| 默认日志目录 | `D:/SpinTale/logs`，可通过 `LOG_PATH` 覆盖 |

初始化数据库：

```powershell
mysql -uroot -p365061 -e "CREATE DATABASE IF NOT EXISTS SpinTale DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;"
mysql -uroot -p365061 SpinTale < sql\spintale_20260417.sql
mysql -uroot -p365061 SpinTale < sql\quartz.sql
```

Windows 上 MySQL 如果启用了 `lower_case_table_names=1`，数据库目录可能显示为小写，但 JDBC URL 仍使用 `SpinTale`。

## 启动后端

先确认 MySQL 和 Redis 已启动，再执行：

```powershell
mvn -q -DskipTests compile
mvn -pl spintale-admin -am spring-boot:run
```

也可以先打包再运行：

```powershell
mvn -q -DskipTests package
java -jar spintale-admin\target\spintale-admin.jar
```

启动成功后访问：

| 地址 | 用途 |
| --- | --- |
| `http://localhost:8080/` | 后端首页探测 |
| `http://localhost:8080/swagger-ui.html` | Swagger UI |
| `http://localhost:8080/v3/api-docs` | OpenAPI JSON |
| `http://localhost:8080/druid/` | Druid 监控页 |

Druid 默认登录：

```text
用户名: spintale
密码: 365061
```

初始化账号通常为：

```text
用户名: admin
密码: admin123
```

## IntelliJ IDEA 导入

1. 使用 IDEA 打开 `D:\GitCode\SpinTale1.0\SpinTale`。
2. Project SDK 选择 `C:\Users\v1589\.jdks\ms-17.0.19` 或 IDEA 自带 JBR 17。
3. Maven Home 选择 `D:\Maven`，或使用 IDEA Bundled Maven。
4. 刷新 Maven 项目，确认根 `pom.xml` 下的 6 个模块全部导入。
5. 运行配置选择 `com.spintale.SpinTaleApplication`，Working directory 使用项目根目录。

如果 IDEA 出现 `无法解析符号 'springframework'`，优先执行：

```powershell
mvn -U -q -DskipTests install
```

然后在 IDEA 中执行 Maven Reload，必要时再执行 `File > Invalidate Caches`。

## 与前端联调

前端不在本仓库维护，路径为：

```text
D:\GitCode\SpinTale1.0\SpinTale-Vue
```

联调时保持后端运行在 `http://localhost:8080`，前端代理应指向该地址。跨域、接口前缀和登录 Token 逻辑应以前端项目配置为准。

## 常用命令

```powershell
# 编译全部模块
mvn -q -DskipTests compile

# 安装本地 Maven 依赖，适合 IDEA 解析异常时使用
mvn -U -q -DskipTests install

# 打包后端
mvn -q -DskipTests package

# 只运行启动模块
mvn -pl spintale-admin -am spring-boot:run

# 查看 Git 状态
git status --short --branch
```

## 主要功能

### 基础业务功能

当前后端保留并维护以下基础能力：

- 用户、角色、菜单、部门、岗位管理
- 字典、参数、通知公告管理
- 登录日志、操作日志、在线用户监控
- 服务监控、缓存监控、Druid 数据源监控
- Quartz 定时任务和任务日志
- 代码生成器
- JWT 登录认证和 Spring Security 权限控制
- Redis 缓存和验证码能力
- Springdoc OpenAPI 接口文档

### AI 核心功能 (spintale-ai)

**智能对话系统**
- 长期记忆：跨会话记住用户偏好、重要事实（Redis 持久化）
- 幻觉检测：实时检测并缓解 AI 幻觉，置信度评分
- 上下文管理：智能选择相关上下文，优化 token 使用

**RAG 检索增强生成**
- 多格式解析：PDF、Markdown、Word 文档解析
- 向量检索：基于 Milvus 的向量相似度搜索
- Embedding：支持多种 Embedding 模型 (all-MiniLM-L6-v2)

**Agent 系统**
- ReAct Agent：完整的 Reasoning + Acting 循环
- Temporal 编排：基于 Temporal 的工作流引擎，支持长流程、自动重试
- 工具调用：支持 Weather、HTTP API、文件系统等工具
- 技能管理：可扩展的技能注册与执行框架

**MCP 协议支持**
- 资源管理：标准化资源访问接口
- 工具集成：Model Context Protocol 工具支持
- 提示词模板：可复用的 Prompt 模板系统

**内容生成**
- 多模板支持：文章、广告文案、小说等模板
- 流式输出：支持 SSE 流式响应
- 多模型适配：OpenAI、Azure、Ollama、Anthropic

## 配置维护约定

- 项目名、包名、模块名统一使用 `SpinTale` / `spintale` / `com.spintale`。
- 后端配置前缀统一使用 `spintale`。
- 本仓库只处理后端；前端变更应进入 `SpinTale-Vue` 仓库。
- 开发环境可以使用 README 中的默认密码，生产环境必须通过外部配置覆盖数据库、Redis、JWT 和 Druid 密码。
- 修改模块、端口、数据库、缓存、启动方式或部署方式后，需要同步更新 README。

## 已知待清理项

初始化 SQL 中仍有少量上游示例数据文案，例如部门、公告、测试用户昵称等。它们不影响后端启动，但后续做业务定制时应逐步替换为 SpinTale 自有数据。

## 维护计划

README 应随项目变化持续更新，重点关注：

- Maven 版本、模块增删、包名调整
- 数据库脚本和默认配置变化
- 启动、构建、部署方式变化
- 前后端联调地址和代理变化
- 新增业务模块和重要接口说明
