package com.buywise.modules.document.factory;

import java.io.InputStream;

/**
 * =================================================================================================
 * ARCHITECTURAL DESIGN NOTE: STRATEGY PATTERN & FORMAT-AGNOSTIC EXTRACTION
 * =================================================================================================
 * Design Patterns & Principles:
 * 1. Strategy Pattern (GoF):
 *    - Strategy Interface: {@link DocumentProcessor}
 *    - Concrete Strategies: {@link PdfAndImageDocumentProcessor}, {@link PlainTextDocumentProcessor}
 *    - Context: {@link DocumentStorageService} via {@link DocumentProcessorFactory}
 *    Encapsulates document parsing algorithms, making them interchangeable at runtime based on
 *    the file's MIME type (e.g. application/pdf vs text/plain).
 *
 * 2. Open/Closed Principle (OCP):
 *    - Open for extension: Adding support for docx, xlsx, or epub requires only authoring a new
 *      class implementing this interface and annotating it with @Component.
 *    - Closed for modification: Neither the storage service, controller, nor AI pipeline requires edits.
 *
 * 3. Single Responsibility Principle (SRP):
 *    - Each processor focuses solely on extracting raw text bytes from its specific file format.
 *      It has zero knowledge of database persistence, AI embeddings, or user security contexts.
 * =================================================================================================
 */
public interface DocumentProcessor {

    boolean supports(String contentType);

    String extractText(InputStream inputStream, String filename) throws Exception;
}
