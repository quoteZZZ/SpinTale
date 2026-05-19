package com.spintale.ai.retrieval.document;

import com.spintale.ai.retrieval.document.DocumentMetadata;
import com.spintale.ai.retrieval.document.DocumentParser;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

/**
 * Markdown 文档解析器实现
 * 支持 .md 和 .markdown 格式
 */
public class MarkdownDocumentParser implements DocumentParser {

    @Override
    public List<Document> parse(InputStream inputStream, DocumentMetadata metadata) {
        try {
            String text = IOUtils.toString(inputStream, StandardCharsets.UTF_8);

            Metadata langchainMetadata = new Metadata();
            langchainMetadata.put("source", metadata.getSource());
            langchainMetadata.put("filename", metadata.getFilename());
            langchainMetadata.put("file_type", "markdown");

            Document doc = Document.from(text, langchainMetadata);
            return Collections.singletonList(doc);
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse Markdown document: " + metadata.getFilename(), e);
        }
    }

    @Override
    public String[] getSupportedFormats() {
        return new String[]{"md", "markdown", "MD", "MARKDOWN"};
    }
}
