package com.buywise.modules.product.controller;

import com.buywise.modules.product.dto.CreateSellerRequest;
import com.buywise.modules.product.dto.ProductCategoryDto;
import com.buywise.modules.product.dto.SellerDto;
import com.buywise.modules.product.service.ProductCatalogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/catalog")
@RequiredArgsConstructor
@Tag(name = "Catalog", description = "Product categories and seller registry endpoints")
public class CatalogController {

    private final ProductCatalogService catalogService;

    @GetMapping("/categories")
    @Operation(summary = "List all supported product categories")
    public ResponseEntity<List<ProductCategoryDto>> getCategories() {
        return ResponseEntity.ok(catalogService.getAllCategories());
    }

    @GetMapping("/sellers")
    @Operation(summary = "List all registered retail sellers")
    public ResponseEntity<List<SellerDto>> getSellers() {
        return ResponseEntity.ok(catalogService.getAllSellers());
    }

    @PostMapping("/sellers")
    @Operation(summary = "Register a new seller")
    public ResponseEntity<SellerDto> createSeller(@Valid @RequestBody CreateSellerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(catalogService.createSeller(request));
    }
}
