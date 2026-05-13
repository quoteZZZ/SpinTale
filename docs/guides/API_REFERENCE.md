# AI 模块 API 接口文档

## 概述

本文档描述了 Spintale AI 模块为前端提供的 RESTful API 接口。所有接口均基于 Spring Boot 实现，支持标准的 HTTP 协议。

**基础路径**: `/ai`

---

## 1. 聊天接口 (`/ai/chat`)

### 1.1 发送消息（非流式）

**接口**: `POST /ai/chat/message`

**请求体**:
```json
{
  "sessionId": "可选，会话 ID",
  "message": "用户消息内容",
  "systemPrompt": "可选，系统提示词",
  "temperature": 0.7,
  "maxTokens": 2048,
  "stream": false,
  "enabledTools": ["get_weather"],
  "extraParams": {}
}
```

**响应**:
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "sessionId": "xxx-xxx-xxx",
    "content": "AI 回复内容",
    "model": "gpt-3.5-turbo",
    "tokenUsage": {
      "inputTokens": 50,
      "outputTokens": 100,
      "totalTokens": 150
    },
    "toolCalls": [
      {
        "id": "call_123",
        "name": "get_weather",
        "arguments": "{\"city\": \"北京\"}",
        "result": "{\"temperature\": 25.0, \"condition\": \"晴\"}"
      }
    ],
    "finished": true,
    "extraData": {}
  }
}
```

### 1.2 发送消息（流式）

**接口**: `GET /ai/chat/stream?message=xxx&sessionId=xxx&systemPrompt=xxx&temperature=0.7&maxTokens=2048`

**响应类型**: `text/event-stream`

**SSE 事件**:
- `token`: 实时返回生成的 token
- `complete`: 生成完成，返回完整内容
- `error`: 发生错误

**前端示例**:
```javascript
const eventSource = new EventSource('/ai/chat/stream?message=你好');
eventSource.addEventListener('token', (e) => {
  console.log('Token:', e.data);
});
eventSource.addEventListener('complete', (e) => {
  console.log('Complete:', e.data);
  eventSource.close();
});
eventSource.addEventListener('error', (e) => {
  console.error('Error:', e.data);
  eventSource.close();
});
```

### 1.3 获取会话历史

**接口**: `GET /ai/chat/history/{sessionId}`

**响应**:
```json
{
  "code": 200,
  "data": {
    "sessionId": "xxx",
    "userId": "user123",
    "createdAt": "2024-01-01T10:00:00",
    "lastActiveAt": "2024-01-01T12:00:00",
    "messageCount": 10,
    "messages": [
      {"role": "user", "content": "你好", "timestamp": "2024-01-01T10:00:00"},
      {"role": "assistant", "content": "你好！有什么可以帮助你的？", "timestamp": "2024-01-01T10:00:05"}
    ]
  }
}
```

### 1.4 清除会话历史

**接口**: `DELETE /ai/chat/history/{sessionId}`

### 1.5 列出活跃会话

**接口**: `GET /ai/chat/sessions`

### 1.6 获取可用工具列表

**接口**: `GET /ai/chat/tools`

---

## 2. MCP 接口 (`/ai/mcp`)

### 2.1 获取服务器信息

**接口**: `GET /ai/mcp/info`

**响应**:
```json
{
  "code": 200,
  "data": {
    "name": "spintale-mcp",
    "version": "1.0.0"
  }
}
```

### 2.2 列出资源

**接口**: `GET /ai/mcp/resources`

**响应**:
```json
{
  "code": 200,
  "data": [
    {
      "uri": "file:///docs/readme.md",
      "name": "README 文档",
      "description": "项目说明文档",
      "mimeType": "text/markdown"
    }
  ]
}
```

### 2.3 读取资源

**接口**: `GET /ai/mcp/resources/read?uri=file:///docs/readme.md&params={}`

### 2.4 列出工具

**接口**: `GET /ai/mcp/tools`

**响应**:
```json
{
  "code": 200,
  "data": [
    {
      "id": "get_weather",
      "name": "天气查询",
      "description": "查询指定城市的当前天气情况",
      "inputSchema": {
        "type": "object",
        "properties": {
          "city": {"type": "string", "description": "城市名称"}
        },
        "required": ["city"]
      }
    }
  ]
}
```

### 2.5 调用工具

**接口**: `POST /ai/mcp/tools/call?toolId=get_weather`

**请求体**:
```json
{"city": "北京"}
```

**响应**:
```json
{
  "code": 200,
  "data": {
    "toolId": "get_weather",
    "content": "{\"temperature\": 25.0, \"condition\": \"晴\", \"humidity\": 60}",
    "mimeType": "application/json"
  }
}
```

### 2.6 列出提示词模板

**接口**: `GET /ai/mcp/prompts`

### 2.7 生成提示词

**接口**: `POST /ai/mcp/prompts/generate?promptId=xxx`

---

## 3. RAG 知识库接口 (`/ai/rag`)

### 3.1 检索知识库

**接口**: `POST /ai/rag/query`

**请求体**:
```json
{
  "query": "如何配置项目？",
  "maxResults": 5,
  "minScore": 0.5,
  "knowledgeBaseIds": ["kb1", "kb2"]
}
```

**响应**:
```json
{
  "code": 200,
  "data": {
    "results": [
      {
        "content": "项目配置步骤如下：...",
        "score": 0.95,
        "documentId": "doc123",
        "documentName": "配置指南.md",
        "position": 1,
        "metadata": {"page": 1, "section": "2.1"}
      }
    ],
    "queryTimeMs": 150
  }
}
```

### 3.2 上传文档

**接口**: `POST /ai/rag/upload`

**请求类型**: `multipart/form-data`

**参数**:
- `file`: 文件（支持 PDF、Markdown、TXT、Word）
- `knowledgeBaseId`: 知识库 ID（可选）
- `metadata`: 元数据 JSON（可选）

### 3.3 列出知识库

**接口**: `GET /ai/rag/knowledge-bases`

### 3.4 删除知识库

**接口**: `DELETE /ai/rag/knowledge-bases/{kbId}`

---

## 4. 内容生成接口 (`/ai/generate`)

### 4.1 获取支持的内容类型

**接口**: `GET /ai/generate/types`

### 4.2 生成内容

**接口**: `POST /ai/generate/content`

**请求体**:
```json
{
  "contentType": "ARTICLE",
  "title": "人工智能的发展趋势",
  "description": "写一篇关于 AI 发展趋势的文章",
  "apiKey": "可选的 API Key"
}
```

### 4.3 生成文章

**接口**: `POST /ai/generate/article`

### 4.4 生成小说

**接口**: `POST /ai/generate/novel`

### 4.5 生成广告文案

**接口**: `POST /ai/generate/ad-copy`

### 4.6 流式生成

**接口**: `GET /ai/generate/stream?contentType=article&title=xxx&description=xxx`

---

## 5. 记忆管理接口 (`/ai/memory`)

### 5.1 获取长期记忆

**接口**: `GET /ai/memory/long-term?userId=xxx&category=xxx&limit=10`

### 5.2 添加记忆

**接口**: `POST /ai/memory/long-term`

**请求体**:
```json
{
  "userId": "user123",
  "content": "用户喜欢喝咖啡",
  "category": "preference",
  "importance": 0.8
}
```

### 5.3 删除记忆

**接口**: `DELETE /ai/memory/long-term/{memoryId}`

### 5.4 搜索记忆

**接口**: `POST /ai/memory/search`

**请求体**:
```json
{
  "query": "用户的喜好",
  "userId": "user123",
  "maxResults": 5
}
```

---

## 6. 技能管理接口 (`/ai/skill`)

### 6.1 列出技能

**接口**: `GET /ai/skill/list`

### 6.2 注册技能

**接口**: `POST /ai/skill/register`

### 6.3 注销技能

**接口**: `DELETE /ai/skill/unregister/{skillId}`

### 6.4 执行技能

**接口**: `POST /ai/skill/execute/{skillId}`

---

## 7. 插件管理接口 (`/ai/plugin`)

### 7.1 列出插件

**接口**: `GET /ai/plugin/list`

### 7.2 安装插件

**接口**: `POST /ai/plugin/install`

### 7.3 卸载插件

**接口**: `DELETE /ai/plugin/uninstall/{pluginId}`

### 7.4 启用/禁用插件

**接口**: `PUT /ai/plugin/{pluginId}/enable`
**接口**: `PUT /ai/plugin/{pluginId}/disable`

---

## 错误处理

所有接口统一使用 `AjaxResult` 格式返回：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {}
}
```

**常见错误码**:
- `200`: 成功
- `500`: 服务器内部错误
- `400`: 请求参数错误
- `404`: 资源不存在

---

## 认证与授权

（根据项目实际认证机制补充）

---

## 限流与配额

（根据项目实际需求配置）
