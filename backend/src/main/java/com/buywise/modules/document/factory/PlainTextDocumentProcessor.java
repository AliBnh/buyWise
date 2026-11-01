package com.buywise.modules.document.factory;

import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/**
 * Concrete processor for text, markdown, CSV, and plain receipt formats.
 */
@Component
public class PlainTextDocumentProcessor implements DocumentProcessor {

    @Override
    public boolean supports(String contentType) {
        if (contentType == null) return false;
        String lower = contentType.toLowerCase();
        return lower.contains("text/plain") ||
               lower.contains("text/csv") ||
               lower.contains("text/markdown") ||
               lower.contains("application/json");
    }

    @Override
    public String extractText(InputStream inputStream, String filename) throws Exception {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }
}
