package com.buywise.modules.product.service;

import com.buywise.modules.product.dto.CreateSellerRequest;
import com.buywise.modules.product.dto.ProductCategoryDto;
import com.buywise.modules.product.dto.SellerDto;
import com.buywise.modules.product.model.ProductCategory;
import com.buywise.modules.product.model.Seller;
import com.buywise.modules.product.repository.ProductCategoryRepository;
import com.buywise.modules.product.repository.SellerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductCatalogService {

    private final ProductCategoryRepository categoryRepository;
    private final SellerRepository sellerRepository;

    @Transactional(readOnly = true)
    public List<ProductCategoryDto> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::mapCategoryToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SellerDto> getAllSellers() {
        return sellerRepository.findAll().stream()
                .map(this::mapSellerToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public SellerDto getOrCreateSeller(String sellerName) {
        String trimmed = sellerName.trim();
        return sellerRepository.findByNameIgnoreCase(trimmed)
                .map(this::mapSellerToDto)
                .orElseGet(() -> {
                    Seller newSeller = Seller.builder()
                            .name(trimmed)
                            .reputationScore(new BigDecimal("4.00"))
                            .isVerified(false)
                            .build();
                    return mapSellerToDto(sellerRepository.save(newSeller));
                });
    }

    @Transactional
    public SellerDto createSeller(CreateSellerRequest request) {
        Seller seller = Seller.builder()
                .name(request.getName().trim())
                .websiteUrl(request.getWebsiteUrl())
                .reputationScore(request.getReputationScore() != null ? request.getReputationScore() : new BigDecimal("4.00"))
                .returnPolicySummary(request.getReturnPolicySummary())
                .isVerified(true)
                .build();
        return mapSellerToDto(sellerRepository.save(seller));
    }

    private ProductCategoryDto mapCategoryToDto(ProductCategory cat) {
        return ProductCategoryDto.builder()
                .id(cat.getId())
                .name(cat.getName())
                .slug(cat.getSlug())
                .description(cat.getDescription())
                .iconName(cat.getIconName())
                .build();
    }

    private SellerDto mapSellerToDto(Seller seller) {
        return SellerDto.builder()
                .id(seller.getId())
                .name(seller.getName())
                .websiteUrl(seller.getWebsiteUrl())
                .reputationScore(seller.getReputationScore())
                .returnPolicySummary(seller.getReturnPolicySummary())
                .isVerified(seller.getIsVerified())
                .build();
    }
}
