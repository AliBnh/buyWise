package com.buywise.modules.document.factory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.InputStream;

/**
 * Concrete processor for PDF and image documents.
 * 
 * Demonstrates:
 * - Strategy Implementation: Encapsulates byte stream scanning and printable character extraction for PDF and image formats.
 * - Format Support: Detects application/pdf and image/* MIME types.
 * - Resilient Text Fallback: If OCR/stream has sparse printable characters (<30 bytes), generates a structured
 *   attachment placeholder to prevent blank document states.
 */
@Slf4j
@Component
public class PdfAndImageDocumentProcessor implements DocumentProcessor {

    @Override
    public boolean supports(String contentType) {
        if (contentType == null) return false;
        String lower = contentType.toLowerCase();
        return lower.contains("pdf") ||
               lower.contains("image/png") ||
               lower.contains("image/jpeg") ||
               lower.contains("image/webp");
    }

    @Override
    public String extractText(InputStream inputStream, String filename) throws Exception {
        byte[] bytes = inputStream.readAllBytes();
        
        // Scan for plain readable text segments embedded in the document stream
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("[DOCUMENT HEADER: %s, Byte Size: %d]\n", filename, bytes.length));
        
        StringBuilder currentLine = new StringBuilder();
        int printableCount = 0;

        for (byte b : bytes) {
            if (b >= 32 && b <= 126) { // Printable ASCII range
                currentLine.append((char) b);
                printableCount++;
            } else if (b == '\n' || b == '\r') {
                if (currentLine.length() > 5) {
                    sb.append(currentLine.toString().trim()).append("\n");
                }
                currentLine.setLength(0);
            }
        }

        if (currentLine.length() > 5) {
            sb.append(currentLine.toString().trim()).append("\n");
        }

        String extracted = sb.toString().trim();
        if (printableCount < 30) {
            return String.format("[DOCUMENT ATTACHMENT: %s]\nBinary document file stored successfully for verification.", filename);
        }

        return extracted;
    }
}
