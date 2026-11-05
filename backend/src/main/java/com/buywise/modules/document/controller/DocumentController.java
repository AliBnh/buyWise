package com.buywise.modules.document.controller;

import com.buywise.modules.document.dto.DocumentDetailResponse;
import com.buywise.modules.document.dto.DocumentUploadResponse;
import com.buywise.modules.document.dto.ExtractedFactDto;
import com.buywise.modules.document.dto.FactVerificationRequest;
import com.buywise.modules.document.model.DocumentType;
import com.buywise.modules.document.service.DocumentStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * REST controller managing document ingestion, binary asset streaming, and Human-in-the-Loop (HITL) fact verification.
 * 
 * Demonstrates:
 * - Multipart Streaming Ingestion: Handles binary multipart upload with size limits (15MB) and mime-type validation.
 * - Human-in-the-Loop (HITL) Design Pattern: Directly enables consumers to inspect, approve, or correct AI-extracted attributes.
 * - Path Traversal & IDOR Defense: Verifies tenant ownership before resolving server filesystem storage paths.
 */
@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
@Tag(name = "Documents & Fact Extraction", description = "Endpoints for uploading receipts, invoices, and verifying AI-extracted facts")
public class DocumentController {

    private final DocumentStorageService storageService;

    /**
     * Uploads an invoice, receipt, or warranty PDF/image, triggers OCR/parsing, and extracts structured purchase facts.
     * 
     * @param file Binary file payload received via multipart/form-data.
     * @param documentType Optional classification hint (RECEIPT, INVOICE, WARRANTY).
     * @param purchaseId Optional linked purchase identifier.
     * @param comparisonId Optional linked comparison identifier.
     * @return HTTP 201 CREATED with stored document metadata and extraction task status.
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a document (receipt, invoice, warranty) for storage and AI fact extraction")
    public ResponseEntity<DocumentUploadResponse> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "documentType", required = false, defaultValue = "RECEIPT") DocumentType documentType,
            @RequestParam(value = "purchaseId", required = false) Long purchaseId,
            @RequestParam(value = "comparisonId", required = false) Long comparisonId) {

        DocumentUploadResponse response = storageService.storeAndProcessDocument(file, documentType, purchaseId, comparisonId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Lists all documents uploaded by the calling user.
     * 
     * @return HTTP 200 OK with document summaries.
     */
    @GetMapping
    @Operation(summary = "List all documents uploaded by the current user")
    public ResponseEntity<List<DocumentUploadResponse>> getUserDocuments() {
        return ResponseEntity.ok(storageService.getUserDocuments());
    }

    /**
     * Retrieves full document metadata including raw text excerpt, extraction status, and all extracted semantic facts.
     * 
     * @param id Target document ID.
     * @return HTTP 200 OK with detailed document and fact response.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get detailed document metadata and all extracted facts with confidence scores")
    public ResponseEntity<DocumentDetailResponse> getDocumentDetail(@PathVariable Long id) {
        return ResponseEntity.ok(storageService.getDocumentDetail(id));
    }

    /**
     * Streams the stored binary file back to the client as an attachment, with filename sanitization.
     * 
     * @param id Document ID.
     * @return HTTP 200 OK with binary octet stream and Content-Disposition header.
     */
    @GetMapping("/{id}/download")
    @Operation(summary = "Download stored document binary securely")
    public ResponseEntity<Resource> downloadDocument(@PathVariable Long id) {
        Resource resource = storageService.loadFileAsResource(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    /**
     * Approves, modifies, or rejects an AI-extracted fact, implementing the Human-in-the-Loop (HITL) architectural pattern.
     * Optionally propagates verified values to linked purchase or offer records.
     * 
     * @param factId Unique identifier of the extracted fact.
     * @param request Verification status and optional user-edited value.
     * @return HTTP 200 OK with updated fact verification status.
     */
    @PatchMapping("/facts/{factId}/verify")
    @Operation(summary = "Approve, correct, or reject an AI-extracted fact (Human-in-the-Loop)")
    public ResponseEntity<ExtractedFactDto> verifyFact(
            @PathVariable Long factId,
            @Valid @RequestBody FactVerificationRequest request) {
        return ResponseEntity.ok(storageService.verifyFact(factId, request));
    }

    /**
     * Deletes stored physical document file and cascades deletion of extracted facts.
     * 
     * @param id Document ID.
     * @return HTTP 204 NO CONTENT.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete uploaded document and its extracted facts")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        storageService.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }
}
