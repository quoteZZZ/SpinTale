package com.spintale.ai.web.controller;

import com.spintale.ai.mcp.McpServer;
import com.spintale.ai.mcp.McpTool;
import com.spintale.ai.mcp.McpResource;
import com.spintale.common.core.domain.AjaxResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP 服务器控制器
 * 提供 MCP 资源、工具、提示词的管理和调用接口
 */
@RestController
@RequestMapping("/ai/mcp")
public class McpController {

    private static final Logger log = LoggerFactory.getLogger(McpController.class);

    private final McpServer mcpServer;

    public McpController(McpServer mcpServer) {
        this.mcpServer = mcpServer;
    }

    /**
     * 获取服务器信息
     */
    @GetMapping("/info")
    public AjaxResult getServerInfo() {
        McpServer.ServerInfo info = mcpServer.getServerInfo();
        return AjaxResult.success(Map.of(
                "name", info.getName(),
                "version", info.getVersion()
        ));
    }

    /**
     * 列出所有资源
     */
    @GetMapping("/resources")
    public AjaxResult listResources() {
        try {
            List<McpResource> resources = mcpServer.listResources();
            var resourceList = resources.stream()
                    .map(r -> Map.of(
                            "uri", r.getUri(),
                            "name", r.getName(),
                            "description", r.getDescription(),
                            "mimeType", r.getMimeType()
                    ))
                    .toList();
            return AjaxResult.success(resourceList);
        } catch (Exception e) {
            log.error("Failed to list resources", e);
            return AjaxResult.error("获取资源列表失败：" + e.getMessage());
        }
    }

    /**
     * 读取资源内容
     */
    @GetMapping("/resources/read")
    public AjaxResult readResource(
            @RequestParam String uri,
            @RequestParam(required = false) Map<String, Object> params) {
        try {
            McpResource.ResourceContent content = mcpServer.readResource(uri, params != null ? params : new HashMap<>());
            return AjaxResult.success(Map.of(
                    "uri", uri,
                    "mimeType", content.getMimeType(),
                    "text", content.getText(),
                    "blob", content.getBlob() // Base64 编码的二进制数据
            ));
        } catch (Exception e) {
            log.error("Failed to read resource: {}", uri, e);
            return AjaxResult.error("读取资源失败：" + e.getMessage());
        }
    }

    /**
     * 列出所有工具
     */
    @GetMapping("/tools")
    public AjaxResult listTools() {
        try {
            List<McpTool> tools = mcpServer.listTools();
            var toolList = tools.stream()
                    .map(t -> {
                        var inputSchema = t.getInputSchema();
                        return Map.of(
                                "id", t.getId(),
                                "name", t.getName(),
                                "description", t.getDescription(),
                                "inputSchema", inputSchema
                        );
                    })
                    .toList();
            return AjaxResult.success(toolList);
        } catch (Exception e) {
            log.error("Failed to list tools", e);
            return AjaxResult.error("获取工具列表失败：" + e.getMessage());
        }
    }

    /**
     * 调用工具
     */
    @PostMapping("/tools/call")
    public AjaxResult callTool(
            @RequestParam String toolId,
            @RequestBody Map<String, Object> args) {
        try {
            log.info("Calling MCP tool: {} with args: {}", toolId, args);
            McpTool.ToolResult result = mcpServer.callTool(toolId, args != null ? args : new HashMap<>());
            
            if (result.isError()) {
                return AjaxResult.error(result.getContent().getText());
            }
            
            return AjaxResult.success(Map.of(
                    "toolId", toolId,
                    "content", result.getContent().getText(),
                    "mimeType", result.getContent().getMimeType()
            ));
        } catch (Exception e) {
            log.error("Failed to call tool: {}", toolId, e);
            return AjaxResult.error("调用工具失败：" + e.getMessage());
        }
    }

    /**
     * 列出所有提示词模板
     */
    @GetMapping("/prompts")
    public AjaxResult listPrompts() {
        try {
            var prompts = mcpServer.listPrompts();
            var promptList = prompts.stream()
                    .map(p -> {
                        var args = p.getArguments().stream()
                                .map(a -> Map.of(
                                        "name", a.getName(),
                                        "description", a.getDescription(),
                                        "required", a.isRequired(),
                                        "defaultValue", a.getDefaultValue()
                                ))
                                .toList();
                        return Map.of(
                                "id", p.getId(),
                                "name", p.getName(),
                                "description", p.getDescription(),
                                "arguments", args
                        );
                    })
                    .toList();
            return AjaxResult.success(promptList);
        } catch (Exception e) {
            log.error("Failed to list prompts", e);
            return AjaxResult.error("获取提示词列表失败：" + e.getMessage());
        }
    }

    /**
     * 生成提示词
     */
    @PostMapping("/prompts/generate")
    public AjaxResult generatePrompt(
            @RequestParam String promptId,
            @RequestBody Map<String, Object> args) {
        try {
            String prompt = mcpServer.generatePrompt(promptId, args != null ? args : new HashMap<>());
            return AjaxResult.success(Map.of(
                    "promptId", promptId,
                    "content", prompt
            ));
        } catch (Exception e) {
            log.error("Failed to generate prompt: {}", promptId, e);
            return AjaxResult.error("生成提示词失败：" + e.getMessage());
        }
    }
}
