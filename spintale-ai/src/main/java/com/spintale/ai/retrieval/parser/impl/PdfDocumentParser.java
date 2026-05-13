package com.spintale.ai.retrieval.parser.impl;

import com.spintale.ai.retrieval.parser.DocumentMetadata;
import com.spintale.ai.retrieval.parser.DocumentParser;
import dev.langchain4j.data.document.Document;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

/**
 * PDF 文档解析器实现
 * 使用 Apache PDFBox 解析 PDF 文件
 */
public class PdfDocumentParser implements DocumentParser {

    @Override
    public List<Document> parse(InputStream inputStream, DocumentMetadata metadata) {
        try {
            PDDocument pdfDocument = PDDocument.load(inputStream);
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(pdfDocument);
            pdfDocument.close();

            dev.langchain4j.data.document.Metadata langchainMetadata = 
                dev.langchain4j.data.document.Metadata.from(metadata.getCustomMetadata());
            langchainMetadata.add("source", metadata.getSource());
            langchainMetadata.add("filename", metadata.getFilename());

            Document document = Document.from(text, langchainMetadata);
            return Collections.singletonList(document);
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse PDF document: " + metadata.getFilename(), e);
        }
    }

    @Override
    public String[] getSupportedFormats() {
        return new String[]{"pdf", "PDF"};
    }
}
