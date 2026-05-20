package com.spintale.ai.agent.tool.mcp.builtin;

import com.spintale.ai.infrastructure.properties.McpProperties;
import com.spintale.ai.agent.tool.mcp.core.McpResource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * MCP resource for safe read-only access under a configured base directory.
 */
@Service
@ConditionalOnProperty(prefix = "spintale.ai.mcp", name = "enabled", havingValue = "true")
public class McpFileResource implements McpResource {

    private static final String BASE_URI = "mcp://filesystem";

    private final Path basePath;
    private final long maxFileSizeBytes;

    public McpFileResource(McpProperties properties) {
        this(resolveBasePath(properties), resolveMaxFileSize(properties));
    }

    public McpFileResource(Path basePath) {
        this(basePath, 1024L * 1024L);
    }

    public McpFileResource(Path basePath, long maxFileSizeBytes) {
        Path path = basePath == null ? Paths.get(System.getProperty("user.dir")) : basePath;
        this.basePath = path.toAbsolutePath().normalize();
        this.maxFileSizeBytes = Math.max(1024L, maxFileSizeBytes);
    }

    @Override
    public String getUri() {
        return BASE_URI;
    }

    @Override
    public String getName() {
        return "filesystem";
    }

    @Override
    public String getDescription() {
        return "Read text files from the project directory with path traversal protection.";
    }

    @Override
    public String getMimeType() {
        return "text/plain";
    }

    @Override
    public ResourceContent read(String uri, Map<String, Object> params) {
        try {
            if (uri == null || !uri.startsWith(BASE_URI)) {
                return error(uri, "Invalid filesystem resource URI");
            }

            String pathStr = uri.substring(BASE_URI.length());
            if (pathStr.startsWith("/") || pathStr.startsWith("\\")) {
                pathStr = pathStr.substring(1);
            }
            pathStr = URLDecoder.decode(pathStr, StandardCharsets.UTF_8);
            if (pathStr.isBlank()) {
                return error(uri, "File path is required");
            }

            Path candidate = basePath.resolve(pathStr).normalize();
            if (!candidate.startsWith(basePath)) {
                return error(uri, "Access denied - path outside allowed directory");
            }

            if (!Files.exists(candidate)) {
                return error(uri, "File not found - " + pathStr);
            }
            if (!Files.isRegularFile(candidate)) {
                return error(uri, "Not a regular file - " + pathStr);
            }

            Path realBase = basePath.toRealPath();
            Path realFile = candidate.toRealPath();
            if (!realFile.startsWith(realBase)) {
                return error(uri, "Access denied - symlink outside allowed directory");
            }

            long size = Files.size(realFile);
            if (size > maxFileSizeBytes) {
                return error(uri, "File too large (max 1MB): " + size + " bytes");
            }

            String mimeType = detectMimeType(realFile);
            String content = Files.readString(realFile, StandardCharsets.UTF_8);
            ResourceContent resourceContent = ResourceContent.text(uri, mimeType, content);

            ResourceMetadata metadata = new ResourceMetadata();
            metadata.setUri(uri);
            metadata.setName(realFile.getFileName().toString());
            metadata.setDescription("File: " + pathStr);
            metadata.setMimeType(mimeType);
            metadata.setSize(size);
            metadata.setLastModified(Files.getLastModifiedTime(realFile).toMillis());
            resourceContent.setMetadata(metadata);

            return resourceContent;
        } catch (IOException e) {
            return error(uri, "Error reading file: " + e.getMessage());
        }
    }

    private ResourceContent error(String uri, String message) {
        return ResourceContent.text(uri == null ? BASE_URI : uri, "text/plain", "Error: " + message);
    }

    private String detectMimeType(Path path) {
        String name = path.toString().toLowerCase();
        if (name.endsWith(".java")) {
            return "text/x-java-source";
        }
        if (name.endsWith(".xml") || name.endsWith(".properties")) {
            return "text/plain";
        }
        if (name.endsWith(".json")) {
            return "application/json";
        }
        if (name.endsWith(".md")) {
            return "text/markdown";
        }
        if (name.endsWith(".html") || name.endsWith(".htm")) {
            return "text/html";
        }
        if (name.endsWith(".css")) {
            return "text/css";
        }
        if (name.endsWith(".js") || name.endsWith(".ts")) {
            return "application/javascript";
        }
        if (name.endsWith(".py")) {
            return "text/x-python";
        }
        if (name.endsWith(".yaml") || name.endsWith(".yml")) {
            return "application/yaml";
        }
        return "text/plain";
    }

    private static Path resolveBasePath(McpProperties properties) {
        if (properties == null || properties.getFilesystemBasePath() == null || properties.getFilesystemBasePath().isBlank()) {
            return Paths.get(System.getProperty("user.dir"));
        }
        return Paths.get(properties.getFilesystemBasePath());
    }

    private static long resolveMaxFileSize(McpProperties properties) {
        return properties == null || properties.getMaxFileSizeBytes() == null
                ? 1024L * 1024L
                : properties.getMaxFileSizeBytes();
    }
}
