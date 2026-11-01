package com.buywise.modules.product.service;

import com.buywise.modules.product.dto.ProductCategoryDto;
import com.buywise.modules.product.dto.SellerDto;
import com.buywise.modules.product.model.ProductCategory;
import com.buywise.modules.product.model.Seller;
import com.buywise.modules.product.repository.ProductCategoryRepository;
import com.buywise.modules.product.repository.SellerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductCatalogServiceTest {

    @Mock
    private ProductCategoryRepository categoryRepository;

    @Mock
    private SellerRepository sellerRepository;

    @InjectMocks
    private ProductCatalogService catalogService;

    @Test
    @DisplayName("Should list all product categories")
    void shouldListAllCategories() {
        ProductCategory cat = ProductCategory.builder()
                .id(1L)
                .name("Smartphones")
                .description("Mobile phones and accessories")
                .iconName("Smartphone")
                .build();

        when(categoryRepository.findAll()).thenReturn(List.of(cat));

        List<ProductCategoryDto> result = catalogService.getAllCategories();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Smartphones");
    }

    @Test
    @DisplayName("Should get existing seller or create new if not found")
    void shouldGetOrCreateSeller() {
        when(sellerRepository.findByNameIgnoreCase("Cosmos")).thenReturn(Optional.empty());
        when(sellerRepository.save(any(Seller.class))).thenAnswer(inv -> {
            Seller s = inv.getArgument(0);
            s.setId(10L);
            return s;
        });

        SellerDto created = catalogService.getOrCreateSeller("Cosmos");

        assertThat(created).isNotNull();
        assertThat(created.getName()).isEqualTo("Cosmos");
        verify(sellerRepository).save(any(Seller.class));
    }
}
