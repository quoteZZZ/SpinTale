package com.spintale.ai.retrieval.parser.impl;

import com.spintale.ai.retrieval.parser.DocumentMetadata;
import com.spintale.ai.retrieval.parser.DocumentParser;
import dev.langchain4j.data.document.Document;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Word 文档解析器实现 (.docx)
 * 使用 Apache POI 解析 Word 文件
 */
public class WordDocumentParser implements DocumentParser {

    @Override
    public List<Document> parse(InputStream inputStream, DocumentMetadata metadata) {
        try {
            XWPFDocument document = new XWPFDocument(inputStream);
            
            String text = document.getParagraphs().stream()
                .map(XWPFParagraph::getText)
                .filter(t -> t != null && !t.trim().isEmpty())
                .collect(Collectors.joining("\n\n"));
            
            document.close();

            dev.langchain4j.data.document.Metadata langchainMetadata = 
                dev.langchain4j.data.document.Metadata.from(metadata.getCustomMetadata());
            langchainMetadata.add("source", metadata.getSource());
            langchainMetadata.add("filename", metadata.getFilename());
            langchainMetadata.add("file_type", "docx");

            Document doc = Document.from(text, langchainMetadata);
            return Collections.singletonList(doc);
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse Word document: " + metadata.getFilename(), e);
        }
    }

    @Override
    public String[] getSupportedFormats() {
        return new String[]{"docx", "DOCX"};
    }
}
