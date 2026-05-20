package com.spintale.ai.agent.tool.mcp.core;

import java.util.Map;

/**
 * MCP (Model Context Protocol) 资源接口
 * 
 * MCP 是一个开放协议，用于标准化 AI 模型与外部数据源和工具的集成
 * 参考：https://modelcontextprotocol.io/
 * 
 * 资源代表 AI 可以访问的数据源，如文件、数据库、API 等
 */
public interface McpResource {
    
    /**
     * 资源唯一 URI 标识
     * 格式：mcp://<provider>/<path>
     */
    String getUri();
    
    /**
     * 资源名称（人类可读）
     */
    String getName();
    
    /**
     * 资源描述
     */
    String getDescription();
    
    /**
     * 资源 MIME 类型
     */
    String getMimeType();
    
    /**
     * 读取资源内容
     * @param uri 资源 URI（支持子路径）
     * @param params 可选参数
     * @return 资源内容
     */
    ResourceContent read(String uri, Map<String, Object> params);
    
    /**
     * 资源元数据
     */
    class ResourceMetadata {
        private String uri;
        private String name;
        private String description;
        private String mimeType;
        private Long size;
        private Long lastModified;
        
        public String getUri() { return uri; }
        public void setUri(String uri) { this.uri = uri; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getMimeType() { return mimeType; }
        public void setMimeType(String mimeType) { this.mimeType = mimeType; }
        public Long getSize() { return size; }
        public void setSize(Long size) { this.size = size; }
        public Long getLastModified() { return lastModified; }
        public void setLastModified(Long lastModified) { this.lastModified = lastModified; }
    }
    
    /**
     * 资源内容
     */
    class ResourceContent {
        private String uri;
        private String mimeType;
        private String text;      // 文本内容
        private byte[] binary;    // 二进制内容
        private ResourceMetadata metadata;
        
        public ResourceContent() {}
        
        public ResourceContent(String uri, String mimeType, String text) {
            this.uri = uri;
            this.mimeType = mimeType;
            this.text = text;
        }
        
        public static ResourceContent text(String uri, String mimeType, String content) {
            return new ResourceContent(uri, mimeType, content);
        }
        
        public static ResourceContent binary(String uri, String mimeType, byte[] content) {
            ResourceContent rc = new ResourceContent();
            rc.uri = uri;
            rc.mimeType = mimeType;
            rc.binary = content;
            return rc;
        }
        
        // Getters and Setters
        public String getUri() { return uri; }
        public void setUri(String uri) { this.uri = uri; }
        public String getMimeType() { return mimeType; }
        public void setMimeType(String mimeType) { this.mimeType = mimeType; }
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
        public byte[] getBinary() { return binary; }
        public void setBinary(byte[] binary) { this.binary = binary; }
        public ResourceMetadata getMetadata() { return metadata; }
        public void setMetadata(ResourceMetadata metadata) { this.metadata = metadata; }
    }
}
