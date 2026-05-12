# AI 模块升级文档 - Skills 和 MCP 增强

## 概述

本次升级参考 Gitee 热门 Java+AI 项目（如 langchain4j-spring-boot-starter、skill-manager 等），对 AI 模块进行了全面优化，重点增强了 **Skills（技能系统）** 和 **MCP（Model Context Protocol）** 支持。

## 新增功能

### 1. Skills 技能系统

#### 核心组件

| 文件 | 说明 |
|------|------|
| `AiSkill.java` | 技能接口定义，支持同步/异步执行、流式输出、参数 Schema |
| `SkillManager.java` | 技能管理器，负责注册、发现、执行和生命周期管理 |
| `CodeExplanationSkill.java` | 示例技能：代码解释 |

#### 特性

- **动态注册/注销**: 运行时添加或移除技能
- **参数 Schema**: JSON Schema 格式定义，帮助 AI 理解参数结构
- **标签分类**: 支持按标签筛选技能
- **事件监听**: 支持技能生命周期事件监听
- **流式执行**: 支持流式输出的技能

#### 使用示例

```java
// 注册技能
skillManager.registerSkill(new CodeExplanationSkill());

// 执行技能
Map<String, Object> args = new HashMap<>();
args.put("code", "public class Hello {}");
args.put("language", "Java");
args.put("explanationLevel", "intermediate");

AiSkill.SkillResult result = skillManager.executeSkill("code_explanation", args);

if (result.isSuccess()) {
    System.out.println(result.getData());
} else {
    System.err.println(result.getErrorMessage());
}

// 流式执行
skillManager.executeSkillStreaming("code_explanation", args, new AiSkill.StreamingHandler() {
    @Override
    public void onToken(String token) {
        System.out.print(token);
    }
    
    @Override
    public void onComplete(AiSkill.SkillResult result) {
        System.out.println("\nCompleted!");
    }
    
    @Override
    public void onError(String error) {
        System.err.println("Error: " + error);
    }
});
```

### 2. MCP (Model Context Protocol) 支持

#### 核心组件

| 文件 | 说明 |
|------|------|
| `McpResource.java` | MCP 资源接口，代表 AI 可访问的数据源 |
| `McpTool.java` | MCP 工具接口，代表 AI 可调用的外部功能 |
| `McpServer.java` | MCP 服务器实现，管理资源、工具和提示词模板 |
| `FileSystemResource.java` | 文件系统资源实现，安全访问本地文件 |
| `HttpApiTool.java` | HTTP API 工具实现，调用外部 API |

#### 特性

- **标准化协议**: 遵循 Model Context Protocol 规范
- **资源管理**: 统一访问文件、数据库、API 等数据源
- **工具调用**: 标准化的工具执行接口
- **安全检查**: 路径遍历防护、URL 白名单、大小限制
- **提示词模板**: 支持动态提示词生成

#### 使用示例

```java
// 注册资源和工具
mcpServer.registerResource(new FileSystemResource());
mcpServer.registerTool(new HttpApiTool());

// 读取文件资源
McpResource.ResourceContent content = mcpServer.readResource(
    "mcp://filesystem/src/main/java/App.java", 
    null
);
System.out.println(content.getText());

// 调用 HTTP API 工具
Map<String, Object> args = new HashMap<>();
args.put("url", "https://api.example.com/data");
args.put("method", "GET");
args.put("timeout", 30);

McpTool.ToolResult result = mcpServer.callTool("http_api", args);
System.out.println(result.getContent());
```

## 架构改进

### 之前的问题

1. **工具系统简单**: 只有基础的 `AiTool` 接口，缺乏统一管理
2. **无标准化协议**: 与外部系统集成方式不统一
3. **缺少扩展性**: 难以动态添加新功能
4. **无事件机制**: 无法监控技能执行情况

### 改进方案

1. **分层设计**:
   - Skill 层：高级业务技能
   - MCP 层：标准化协议适配
   - Tool 层：基础工具实现

2. **统一注册中心**: SkillManager 和 McpServer 提供统一的注册和管理

3. **事件驱动**: 支持监听器模式，便于监控和审计

4. **安全增强**: 
   - 文件系统访问限制
   - HTTP URL 白名单检查
   - 请求大小限制

## 配置示例

### application.yml

```yaml
spintale:
  ai:
    # 技能配置
    skills:
      enabled: true
      max-execution-time: 30s
    
    # MCP 配置
    mcp:
      enabled: true
      # 文件系统访问根目录
      filesystem-base-path: /workspace
      # HTTP API 允许的域名白名单
      http-allowed-domains:
        - api.example.com
        - *.github.com
```

## 最佳实践

### 1. 创建自定义技能

```java
@Service
public class WeatherSkill implements AiSkill {
    
    @Override
    public String getId() {
        return "weather_query";
    }
    
    @Override
    public String getName() {
        return "天气查询";
    }
    
    @Override
    public String getDescription() {
        return "查询指定城市的当前天气和预报";
    }
    
    @Override
    public Map<String, Object> getParametersSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");
        
        Map<String, Object> properties = new HashMap<>();
        
        Map<String, Object> cityProp = new HashMap<>();
        cityProp.put("type", "string");
        cityProp.put("description", "城市名称");
        properties.put("city", cityProp);
        
        schema.put("properties", properties);
        schema.put("required", new String[]{"city"});
        
        return schema;
    }
    
    @Override
    public SkillResult execute(Map<String, Object> args) {
        String city = (String) args.get("city");
        // 调用天气 API...
        return SkillResult.success("北京今天晴朗，气温 25°C");
    }
}
```

### 2. 创建自定义 MCP 资源

```java
@Service
public class DatabaseResource implements McpResource {
    
    @Override
    public String getUri() {
        return "mcp://database";
    }
    
    @Override
    public String getName() {
        return "数据库查询";
    }
    
    @Override
    public ResourceContent read(String uri, Map<String, Object> params) {
        String query = (String) params.get("query");
        // 执行 SQL 查询...
        return ResourceContent.text(uri, "application/json", results);
    }
}
```

## 性能优化建议

1. **技能缓存**: 对频繁执行的技能结果进行缓存
2. **连接池**: HTTP 工具使用连接池复用连接
3. **异步执行**: 长时间运行的技能使用异步执行
4. **资源限制**: 设置最大执行时间和内存限制

## 安全注意事项

1. **输入验证**: 严格验证所有用户输入
2. **权限控制**: 基于角色的技能访问控制
3. **审计日志**: 记录所有技能执行和工具调用
4. **速率限制**: 防止滥用和 DDoS 攻击

## 后续计划

- [ ] 集成向量数据库支持长期记忆检索
- [ ] 添加更多预置技能（搜索、计算、翻译等）
- [ ] 实现 MCP 客户端，连接外部 MCP 服务器
- [ ] 支持技能的版本管理和热更新
- [ ] 添加技能市场/商店功能

## 参考项目

- [langchain4j](https://github.com/langchain4j/langchain4j)
- [spring-ai](https://github.com/spring-projects/spring-ai)
- [modelcontextprotocol](https://modelcontextprotocol.io/)
- Gitee 热门 Java+AI 项目
