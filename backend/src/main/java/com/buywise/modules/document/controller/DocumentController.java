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

@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
@Tag(name = "Documents & Fact Extraction", description = "Endpoints for uploading receipts, invoices, and verifying AI-extracted facts")
public class DocumentController {

    private final DocumentStorageService storageService;

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

    @GetMapping
    @Operation(summary = "List all documents uploaded by the current user")
    public ResponseEntity<List<DocumentUploadResponse>> getUserDocuments() {
        return ResponseEntity.ok(storageService.getUserDocuments());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get detailed document metadata and all extracted facts with confidence scores")
    public ResponseEntity<DocumentDetailResponse> getDocumentDetail(@PathVariable Long id) {
        return ResponseEntity.ok(storageService.getDocumentDetail(id));
    }

    @GetMapping("/{id}/download")
    @Operation(summary = "Download stored document binary securely")
    public ResponseEntity<Resource> downloadDocument(@PathVariable Long id) {
        Resource resource = storageService.loadFileAsResource(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @PatchMapping("/facts/{factId}/verify")
    @Operation(summary = "Approve, correct, or reject an AI-extracted fact (Human-in-the-Loop)")
    public ResponseEntity<ExtractedFactDto> verifyFact(
            @PathVariable Long factId,
            @Valid @RequestBody FactVerificationRequest request) {
        return ResponseEntity.ok(storageService.verifyFact(factId, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete uploaded document and its extracted facts")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        storageService.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }
}
