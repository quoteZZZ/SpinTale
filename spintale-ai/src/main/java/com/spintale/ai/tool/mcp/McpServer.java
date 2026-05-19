package com.spintale.ai.tool.mcp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MCP 服务器实现
 * 
 * 提供 MCP 协议的核心功能：
 * - 资源管理（Resources）
 * - 工具管理（Tools）
 * - 提示词模板（Prompts）
 * 
 * 参考：https://modelcontextprotocol.io/
 */
@Service
public class McpServer {
    
    private static final Logger log = LoggerFactory.getLogger(McpServer.class);
    
    private final Map<String, McpResource> resources = new ConcurrentHashMap<>();
    private final Map<String, McpTool> tools = new ConcurrentHashMap<>();
    private final Map<String, McpPrompt> prompts = new ConcurrentHashMap<>();
    
    private final ServerInfo serverInfo;
    
    public McpServer() {
        this.serverInfo = new ServerInfo("spintale-mcp", "1.0.0");
    }
    
    /**
     * 注册资源
     */
    public void registerResource(McpResource resource) {
        resources.put(resource.getUri(), resource);
        log.info("Registered MCP resource: {} ({})", resource.getName(), resource.getUri());
    }
    
    /**
     * 注销资源
     */
    public void unregisterResource(String uri) {
        resources.remove(uri);
        log.info("Unregistered MCP resource: {}", uri);
    }
    
    /**
     * 列出所有资源
     */
    public List<McpResource> listResources() {
        return new ArrayList<>(resources.values());
    }
    
    /**
     * 读取资源
     */
    public McpResource.ResourceContent readResource(String uri, Map<String, Object> params) {
        McpResource resource = resources.get(uri);
        if (resource == null) {
            throw new NoSuchElementException("Resource not found: " + uri);
        }
        return resource.read(uri, params != null ? params : new HashMap<>());
    }
    
    /**
     * 注册工具
     */
    public void registerTool(McpTool tool) {
        tools.put(tool.getId(), tool);
        log.info("Registered MCP tool: {} ({})", tool.getName(), tool.getId());
    }
    
    /**
     * 注销工具
     */
    public void unregisterTool(String toolId) {
        tools.remove(toolId);
        log.info("Unregistered MCP tool: {}", toolId);
    }
    
    /**
     * 列出所有工具
     */
    public List<McpTool> listTools() {
        return new ArrayList<>(tools.values());
    }
    
    /**
     * 调用工具
     */
    public McpTool.ToolResult callTool(String toolId, Map<String, Object> args) {
        McpTool tool = tools.get(toolId);
        if (tool == null) {
            return McpTool.ToolResult.error("Tool not found: " + toolId);
        }
        
        try {
            log.debug("Calling MCP tool: {} with args: {}", toolId, args);
            return tool.execute(args != null ? args : new HashMap<>());
        } catch (Exception e) {
            log.error("MCP tool execution failed: {}", toolId, e);
            return McpTool.ToolResult.error(e.getMessage());
        }
    }
    
    /**
     * 注册提示词模板
     */
    public void registerPrompt(McpPrompt prompt) {
        prompts.put(prompt.getId(), prompt);
        log.info("Registered MCP prompt: {} ({})", prompt.getName(), prompt.getId());
    }
    
    /**
     * 获取提示词模板
     */
    public McpPrompt getPrompt(String id) {
        return prompts.get(id);
    }
    
    /**
     * 列出所有提示词模板
     */
    public List<McpPrompt> listPrompts() {
        return new ArrayList<>(prompts.values());
    }
    
    /**
     * 生成提示词
     */
    public String generatePrompt(String id, Map<String, Object> args) {
        McpPrompt prompt = prompts.get(id);
        if (prompt == null) {
            throw new NoSuchElementException("Prompt not found: " + id);
        }
        return prompt.render(args != null ? args : new HashMap<>());
    }
    
    /**
     * 获取服务器信息
     */
    public ServerInfo getServerInfo() {
        return serverInfo;
    }
    
    /**
     * 服务器信息
     */
    public static class ServerInfo {
        private final String name;
        private final String version;

        public ServerInfo(String name, String version) {
            this.name = name;
            this.version = version;
        }

        public String getName() { return name; }
        public String getVersion() { return version; }
    }
}
