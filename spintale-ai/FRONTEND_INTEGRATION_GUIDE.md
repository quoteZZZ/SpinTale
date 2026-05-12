# AI 模块前端集成指南

## 快速开始

### 1. 基础配置

在 Spring Boot 应用中启用 AI 模块：

```java
@SpringBootApplication
@EnableAiAutoConfig  // 启用 AI 自动配置
public class SpintaleApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpintaleApplication.class, args);
    }
}
```

### 2. 配置文件示例

```yaml
# application.yml
spintale:
  ai:
    # OpenAI 配置
    openai:
      api-key: ${OPENAI_API_KEY:your-api-key}
      base-url: https://api.openai.com/v1
      chat-model: gpt-3.5-turbo
      
    # Ollama 配置（本地模型）
    ollama:
      base-url: http://localhost:11434
      chat-model: llama2
      
    # 长期记忆配置
    memory:
      enabled: true
      max-sessions: 1000
      retention-days: 30
      
    # RAG 配置
    rag:
      enabled: true
      embedding-model: all-minilm-l6-v2
      vector-store: in-memory  # 生产环境建议使用 pgvector
      
    # MCP 配置
    mcp:
      enabled: true
      auto-discover: true
```

---

## 前端集成示例

### Vue 3 示例

#### 1. 聊天组件

```vue
<template>
  <div class="chat-container">
    <div class="messages">
      <div v-for="msg in messages" :key="msg.id" :class="['message', msg.role]">
        <div class="content">{{ msg.content }}</div>
      </div>
    </div>
    <div class="input-area">
      <input v-model="inputMessage" @keyup.enter="sendMessage" placeholder="输入消息..." />
      <button @click="sendMessage" :disabled="loading">发送</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const messages = ref([])
const inputMessage = ref('')
const loading = ref(false)
const sessionId = ref(null)

// 发送消息
async function sendMessage() {
  if (!inputMessage.value.trim() || loading.value) return
  
  const userMsg = {
    id: Date.now(),
    role: 'user',
    content: inputMessage.value
  }
  messages.value.push(userMsg)
  
  loading.value = true
  
  try {
    const response = await fetch('/ai/chat/message', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        sessionId: sessionId.value,
        message: inputMessage.value,
        stream: false
      })
    })
    
    const result = await response.json()
    if (result.code === 200) {
      sessionId.value = result.data.sessionId
      messages.value.push({
        id: Date.now() + 1,
        role: 'assistant',
        content: result.data.content
      })
    }
  } catch (error) {
    console.error('发送失败:', error)
  } finally {
    loading.value = false
    inputMessage.value = ''
  }
}

// 流式消息
async function sendStreamingMessage() {
  const eventSource = new EventSource(
    `/ai/chat/stream?message=${encodeURIComponent(inputMessage.value)}&sessionId=${sessionId.value || ''}`
  )
  
  let assistantMsg = { id: Date.now(), role: 'assistant', content: '' }
  messages.value.push(assistantMsg)
  
  eventSource.addEventListener('token', (e) => {
    assistantMsg.content += e.data
    // 更新 UI
  })
  
  eventSource.addEventListener('complete', () => {
    eventSource.close()
  })
  
  eventSource.addEventListener('error', (e) => {
    console.error('流式错误:', e)
    eventSource.close()
  })
}
</script>
```

#### 2. RAG 知识库组件

```vue
<template>
  <div class="rag-search">
    <input v-model="query" @keyup.enter="search" placeholder="搜索知识库..." />
    <button @click="search">搜索</button>
    
    <div class="results">
      <div v-for="item in results" :key="item.documentId" class="result-item">
        <div class="score">相似度：{{ (item.score * 100).toFixed(1) }}%</div>
        <div class="content">{{ item.content }}</div>
        <div class="meta">{{ item.documentName }} - 位置 {{ item.position }}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const query = ref('')
const results = ref([])

async function search() {
  const response = await fetch('/ai/rag/query', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      query: query.value,
      maxResults: 5,
      minScore: 0.3
    })
  })
  
  const result = await response.json()
  if (result.code === 200) {
    results.value = result.data.results
  }
}
</script>
```

#### 3. MCP 工具调用组件

```vue
<template>
  <div class="tools-panel">
    <h3>可用工具</h3>
    <ul>
      <li v-for="tool in tools" :key="tool.id">
        <div class="tool-info">
          <strong>{{ tool.name }}</strong>
          <p>{{ tool.description }}</p>
        </div>
        <button @click="callTool(tool)">调用</button>
      </li>
    </ul>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const tools = ref([])

onMounted(async () => {
  const response = await fetch('/ai/mcp/tools')
  const result = await response.json()
  if (result.code === 200) {
    tools.value = result.data
  }
})

async function callTool(tool) {
  const args = prompt(`输入参数 (JSON):`, '{}')
  if (!args) return
  
  const response = await fetch(`/ai/mcp/tools/call?toolId=${tool.id}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: args
  })
  
  const result = await response.json()
  if (result.code === 200) {
    alert('工具执行结果:\n' + JSON.stringify(result.data, null, 2))
  } else {
    alert('执行失败:' + result.msg)
  }
}
</script>
```

---

### React 示例

#### 聊天 Hook

```jsx
import { useState, useCallback } from 'react'

export function useAiChat() {
  const [messages, setMessages] = useState([])
  const [sessionId, setSessionId] = useState(null)
  const [loading, setLoading] = useState(false)

  const sendMessage = useCallback(async (message) => {
    setLoading(true)
    
    const userMsg = { id: Date.now(), role: 'user', content: message }
    setMessages(prev => [...prev, userMsg])
    
    try {
      const response = await fetch('/ai/chat/message', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          sessionId,
          message,
          stream: false
        })
      })
      
      const result = await response.json()
      if (result.code === 200) {
        setSessionId(result.data.sessionId)
        setMessages(prev => [...prev, {
          id: Date.now() + 1,
          role: 'assistant',
          content: result.data.content
        }])
      }
    } catch (error) {
      console.error('发送失败:', error)
    } finally {
      setLoading(false)
    }
  }, [sessionId])

  return { messages, sessionId, loading, sendMessage }
}
```

---

## API 接口汇总

| 分类 | 接口 | 方法 | 描述 |
|------|------|------|------|
| 聊天 | `/ai/chat/message` | POST | 发送消息（非流式） |
| 聊天 | `/ai/chat/stream` | GET | 发送消息（流式） |
| 聊天 | `/ai/chat/history/{id}` | GET | 获取会话历史 |
| MCP | `/ai/mcp/tools` | GET | 列出工具 |
| MCP | `/ai/mcp/tools/call` | POST | 调用工具 |
| MCP | `/ai/mcp/resources` | GET | 列出资源 |
| RAG | `/ai/rag/query` | POST | 检索知识库 |
| RAG | `/ai/rag/upload` | POST | 上传文档 |
| 生成 | `/ai/generate/article` | POST | 生成文章 |
| 记忆 | `/ai/memory/long-term` | GET/POST | 管理长期记忆 |

---

## 最佳实践

### 1. 错误处理

```javascript
async function safeApiCall(url, options) {
  try {
    const response = await fetch(url, options)
    const result = await response.json()
    
    if (result.code !== 200) {
      throw new Error(result.msg || '请求失败')
    }
    
    return result.data
  } catch (error) {
    console.error('API 错误:', error)
    // 显示用户友好的错误提示
    throw error
  }
}
```

### 2. 流式响应优化

```javascript
function useSSE(url) {
  const [data, setData] = useState('')
  const [status, setStatus] = useState('connecting')

  useEffect(() => {
    const eventSource = new EventSource(url)
    
    eventSource.addEventListener('token', (e) => {
      setData(prev => prev + e.data)
      setStatus('streaming')
    })
    
    eventSource.addEventListener('complete', () => {
      setStatus('completed')
      eventSource.close()
    })
    
    eventSource.addEventListener('error', () => {
      setStatus('error')
      eventSource.close()
    })
    
    return () => eventSource.close()
  }, [url])

  return { data, status }
}
```

### 3. 会话管理

```javascript
// 使用 localStorage 持久化会话 ID
function usePersistentSession() {
  const [sessionId, setSessionId] = useState(() => {
    return localStorage.getItem('ai_session_id')
  })

  const updateSessionId = (newId) => {
    setSessionId(newId)
    localStorage.setItem('ai_session_id', newId)
  }

  return [sessionId, updateSessionId]
}
```

---

## 性能优化建议

1. **使用流式响应**：减少首字等待时间
2. **会话复用**：避免频繁创建新会话
3. **RAG 缓存**：对热门查询结果进行缓存
4. **工具预加载**：提前加载可用工具列表
5. **错误重试**：实现指数退避重试机制

---

## 安全注意事项

1. **API Key 保护**：不要在前端暴露 API Key
2. **输入验证**：对用户输入进行严格验证
3. **速率限制**：实现请求频率限制
4. **内容过滤**：对 AI 生成内容进行审核
5. **CORS 配置**：正确配置跨域策略
