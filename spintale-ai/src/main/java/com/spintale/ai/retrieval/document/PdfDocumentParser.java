package com.spintale.ai.retrieval.document;

import com.spintale.ai.retrieval.document.DocumentMetadata;
import com.spintale.ai.retrieval.document.DocumentParser;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import org.apache.pdfbox.Loader;
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
            // PDFBox 3.x 使用 Loader 加载文档
            byte[] pdfBytes = inputStream.readAllBytes();
            PDDocument pdfDocument = Loader.loadPDF(pdfBytes);
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(pdfDocument);
            pdfDocument.close();

            Metadata langchainMetadata = new Metadata();
            langchainMetadata.put("source", metadata.getSource());
            langchainMetadata.put("filename", metadata.getFilename());

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
