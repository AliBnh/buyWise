package com.buywise.modules.document.factory;

import java.io.InputStream;

/**
 * Strategy/Processor interface for reading raw textual content from diverse document formats.
 * 
 * Demonstrates:
 * - Strategy Pattern (GoF): Pluggable document text extractors based on MIME content type.
 * - Single Responsibility Principle (SRP): Isolates format-specific parsing from AI extraction logic.
 */
public interface DocumentProcessor {

    boolean supports(String contentType);

    String extractText(InputStream inputStream, String filename) throws Exception;
}
