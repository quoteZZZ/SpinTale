package com.spintale.ai.retrieval.ingestion;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;

/**
 * Resolves an ingestion parser from a filename extension.
 */
@Component
public class DocumentParserRegistry {

    private final Map<String, DocumentParser> parsers = new HashMap<>();

    public DocumentParserRegistry(List<DocumentParser> parserList) {
        if (parserList == null) {
            return;
        }
        for (DocumentParser parser : parserList) {
            for (String format : parser.getSupportedFormats()) {
                if (format != null && !format.isBlank()) {
                    parsers.put(normalize(format), parser);
                }
            }
        }
    }

    public Optional<DocumentParser> findParser(String filename) {
        return Optional.ofNullable(parsers.get(extension(filename)));
    }

    private String extension(String filename) {
        if (filename == null) {
            return "";
        }
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == filename.length() - 1) {
            return "";
        }
        return normalize(filename.substring(dotIndex + 1));
    }

    private String normalize(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
