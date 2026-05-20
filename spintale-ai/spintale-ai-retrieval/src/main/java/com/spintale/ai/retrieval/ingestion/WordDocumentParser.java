package com.spintale.ai.retrieval.ingestion;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Word .docx parser for RAG ingestion.
 */
@Component
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

            Metadata langchainMetadata = new Metadata();
            langchainMetadata.put("source", metadata.getSource());
            langchainMetadata.put("filename", metadata.getFilename());
            langchainMetadata.put("file_type", "docx");

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
