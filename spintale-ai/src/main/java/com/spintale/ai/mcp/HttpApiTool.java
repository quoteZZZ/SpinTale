package com.spintale.ai.mcp;

import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * HTTP API MCP 工具实现
 * 
 * 允许 AI 调用外部 HTTP API
 */
@Service
public class HttpApiTool implements McpTool {
    
    private final HttpClient httpClient;
    
    public HttpApiTool() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }
    
    @Override
    public String getId() {
        return "http_api";
    }
    
    @Override
    public String getName() {
        return "HTTP API 调用";
    }
    
    @Override
    public String getDescription() {
        return "调用外部 HTTP/HTTPS API，支持 GET、POST、PUT、DELETE 等方法";
    }
    
    @Override
    public Map<String, Object> getInputSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");
        
        Map<String, Object> properties = new HashMap<>();
        
        // url
        Map<String, Object> urlProp = new HashMap<>();
        urlProp.put("type", "string");
        urlProp.put("description", "API 端点 URL");
        properties.put("url", urlProp);
        
        // method
        Map<String, Object> methodProp = new HashMap<>();
        methodProp.put("type", "string");
        methodProp.put("description", "HTTP 方法");
        methodProp.put("enum", new String[]{"GET", "POST", "PUT", "DELETE", "PATCH"});
        methodProp.put("default", "GET");
        properties.put("method", methodProp);
        
        // headers
        Map<String, Object> headersProp = new HashMap<>();
        headersProp.put("type", "object");
        headersProp.put("description", "HTTP 请求头");
        properties.put("headers", headersProp);
        
        // body
        Map<String, Object> bodyProp = new HashMap<>();
        bodyProp.put("type", "string");
        bodyProp.put("description", "请求体（JSON 格式）");
        properties.put("body", bodyProp);
        
        // timeout
        Map<String, Object> timeoutProp = new HashMap<>();
        timeoutProp.put("type", "integer");
        timeoutProp.put("description", "超时时间（秒）");
        timeoutProp.put("default", 30);
        properties.put("timeout", timeoutProp);
        
        schema.put("properties", properties);
        schema.put("required", new String[]{"url"});
        
        return schema;
    }
    
    @Override
    public ToolResult execute(Map<String, Object> args) {
        try {
            String urlStr = (String) args.get("url");
            String method = (String) args.getOrDefault("method", "GET");
            Map<String, String> headers = (Map<String, String>) args.get("headers");
            String body = (String) args.get("body");
            int timeout = (Integer) args.getOrDefault("timeout", 30);
            
            if (urlStr == null || urlStr.trim().isEmpty()) {
                return ToolResult.error("URL is required");
            }
            
            // URL 安全检查
            URI uri = new URI(urlStr);
            String scheme = uri.getScheme();
            if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
                return ToolResult.error("Only HTTP/HTTPS URLs are allowed");
            }
            
            // 构建请求
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(uri)
                    .timeout(Duration.ofSeconds(timeout));
            
            // 添加方法
            if ("GET".equalsIgnoreCase(method)) {
                requestBuilder.GET();
            } else if ("POST".equalsIgnoreCase(method)) {
                requestBuilder.POST(body != null ? HttpRequest.BodyPublishers.ofString(body) 
                        : HttpRequest.BodyPublishers.noBody());
            } else if ("PUT".equalsIgnoreCase(method)) {
                requestBuilder.PUT(body != null ? HttpRequest.BodyPublishers.ofString(body) 
                        : HttpRequest.BodyPublishers.noBody());
            } else if ("DELETE".equalsIgnoreCase(method)) {
                requestBuilder.DELETE();
            } else if ("PATCH".equalsIgnoreCase(method)) {
                requestBuilder.method("PATCH", body != null ? HttpRequest.BodyPublishers.ofString(body) 
                        : HttpRequest.BodyPublishers.noBody());
            } else {
                return ToolResult.error("Unsupported HTTP method: " + method);
            }
            
            // 添加请求头
            if (headers != null) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    requestBuilder.header(entry.getKey(), entry.getValue());
                }
            }
            
            // 默认添加 JSON 内容类型（如果有 body）
            if (body != null && headers != null && !headers.containsKey("Content-Type")) {
                requestBuilder.header("Content-Type", "application/json");
            }
            
            // 发送请求
            HttpResponse<String> response = httpClient.send(
                    requestBuilder.build(), 
                    HttpResponse.BodyHandlers.ofString());
            
            // 构建响应
            Map<String, Object> result = new HashMap<>();
            result.put("statusCode", response.statusCode());
            result.put("headers", response.headers().map());
            result.put("body", response.body());
            
            String responseText = String.format(
                    "HTTP %d\n%s",
                    response.statusCode(),
                    response.body());
            
            return ToolResult.success(responseText, "text");
            
        } catch (Exception e) {
            return ToolResult.error("HTTP API call failed: " + e.getMessage());
        }
    }
}
