package com.buywise.modules.document.factory;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Factory resolving the appropriate {@link DocumentProcessor} based on MIME content type.
 * 
 * Demonstrates:
 * - Factory Pattern (GoF): Encapsulates document processor selection logic.
 * - Dependency Injection: Injects all registered DocumentProcessor implementations.
 */
@Component
public class DocumentProcessorFactory {

    private final List<DocumentProcessor> processors;
    private final PlainTextDocumentProcessor defaultFallback;

    public DocumentProcessorFactory(List<DocumentProcessor> processors, PlainTextDocumentProcessor defaultFallback) {
        this.processors = processors;
        this.defaultFallback = defaultFallback;
    }

    public DocumentProcessor getProcessor(String contentType) {
        if (contentType != null) {
            for (DocumentProcessor processor : processors) {
                if (processor.supports(contentType)) {
                    return processor;
                }
            }
        }
        return defaultFallback;
    }
}
