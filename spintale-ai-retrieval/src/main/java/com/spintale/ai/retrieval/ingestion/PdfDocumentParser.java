package com.spintale.ai.retrieval.ingestion;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

/**
 * PDF parser for RAG ingestion.
 */
@Component
public class PdfDocumentParser implements DocumentParser {

    @Override
    public List<Document> parse(InputStream inputStream, DocumentMetadata metadata) {
        try {
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
