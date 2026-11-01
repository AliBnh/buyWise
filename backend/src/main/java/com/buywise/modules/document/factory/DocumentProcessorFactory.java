package com.buywise.modules.document.factory;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * =================================================================================================
 * ARCHITECTURAL DESIGN NOTE: FACTORY PATTERN & SPRING IOC COLLECTION INJECTION
 * =================================================================================================
 * Design Patterns & Principles:
 * 1. Factory Pattern (GoF):
 *    - Encapsulates strategy resolution logic. Client code requests a processor by MIME type
 *      without coupling to concrete subclasses.
 *
 * 2. Spring IoC Collection Injection:
 *    - Spring automatically discovers all beans in the ApplicationContext that implement
 *      {@link DocumentProcessor} and injects them into the {@code List<DocumentProcessor>} constructor parameter.
 *    - No manual registration, XML config, or hardcoded switch-case statements are needed.
 *
 * 3. Graceful Fallback / Null Object Variant:
 *    - If an unrecognized MIME type is supplied, it falls back to {@link PlainTextDocumentProcessor}
 *      rather than throwing NullPointerException, adhering to resilient API design.
 *
 * Technical Interview Q&A Crib Sheet:
 * Q: "How does Spring populate List<DocumentProcessor> without an explicit @Qualifier?"
 * A: When a constructor or field requires List<T>, Spring's DefaultListableBeanFactory inspects the
 *    bean registry for all singleton beans implementing interface T, resolves dependency order,
 *    and injects an unmodifiable ArrayList containing all matching beans.
 * =================================================================================================
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
