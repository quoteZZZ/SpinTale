package com.spintale.ai.mcp;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * 文件系统 MCP 资源实现
 * 
 * 允许 AI 安全地访问指定目录下的文件
 */
@Service
public class FileSystemResource implements McpResource {
    
    private static final String BASE_URI = "mcp://filesystem";
    private final Path basePath;
    
    public FileSystemResource() {
        // 默认限制在项目根目录
        this.basePath = Paths.get(System.getProperty("user.dir"));
    }
    
    public FileSystemResource(Path basePath) {
        this.basePath = basePath;
    }
    
    @Override
    public String getUri() {
        return BASE_URI;
    }
    
    @Override
    public String getName() {
        return "文件系统";
    }
    
    @Override
    public String getDescription() {
        return "访问项目目录下的文件内容，支持文本文件读取";
    }
    
    @Override
    public String getMimeType() {
        return "text/plain";
    }
    
    @Override
    public ResourceContent read(String uri, Map<String, Object> params) {
        try {
            // 解析路径：mcp://filesystem/path/to/file.txt
            String pathStr = uri.replace(BASE_URI, "");
            if (pathStr.startsWith("/")) {
                pathStr = pathStr.substring(1);
            }
            
            Path filePath = basePath.resolve(pathStr).normalize();
            
            // 安全检查：确保路径在 basePath 内
            if (!filePath.startsWith(basePath)) {
                return ResourceContent.text(uri, "text/plain", 
                    "Error: Access denied - path outside allowed directory");
            }
            
            if (!Files.exists(filePath)) {
                return ResourceContent.text(uri, "text/plain", 
                    "Error: File not found - " + pathStr);
            }
            
            if (!Files.isRegularFile(filePath)) {
                return ResourceContent.text(uri, "text/plain", 
                    "Error: Not a regular file - " + pathStr);
            }
            
            // 检查文件大小（限制 1MB）
            long size = Files.size(filePath);
            if (size > 1024 * 1024) {
                return ResourceContent.text(uri, "text/plain", 
                    "Error: File too large (max 1MB): " + size + " bytes");
            }
            
            String content = Files.readString(filePath);
            
            ResourceContent rc = ResourceContent.text(uri, detectMimeType(filePath), content);
            
            ResourceMetadata metadata = new ResourceMetadata();
            metadata.setUri(uri);
            metadata.setName(filePath.getFileName().toString());
            metadata.setDescription("File: " + pathStr);
            metadata.setMimeType(detectMimeType(filePath));
            metadata.setSize(size);
            metadata.setLastModified(Files.getLastModifiedTime(filePath).toMillis());
            rc.setMetadata(metadata);
            
            return rc;
            
        } catch (IOException e) {
            return ResourceContent.text(uri, "text/plain", 
                "Error reading file: " + e.getMessage());
        }
    }
    
    private String detectMimeType(Path path) {
        String name = path.toString().toLowerCase();
        if (name.endsWith(".java") || name.endsWith(".xml") || name.endsWith(".properties")) {
            return "text/xml";
        } else if (name.endsWith(".json")) {
            return "application/json";
        } else if (name.endsWith(".md")) {
            return "text/markdown";
        } else if (name.endsWith(".html") || name.endsWith(".htm")) {
            return "text/html";
        } else if (name.endsWith(".css")) {
            return "text/css";
        } else if (name.endsWith(".js") || name.endsWith(".ts")) {
            return "application/javascript";
        } else if (name.endsWith(".py")) {
            return "text/x-python";
        } else if (name.endsWith(".yaml") || name.endsWith(".yml")) {
            return "application/yaml";
        }
        return "text/plain";
    }
}
