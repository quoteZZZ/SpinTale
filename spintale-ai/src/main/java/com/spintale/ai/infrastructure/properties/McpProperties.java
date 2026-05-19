package com.spintale.ai.infrastructure.properties;

import lombok.Data;

/**
 * MCP server and built-in MCP resource settings.
 */
@Data
public class McpProperties {
    private Boolean enabled = false;
    private String filesystemBasePath;
    private Long maxFileSizeBytes = 1024L * 1024L;
}
