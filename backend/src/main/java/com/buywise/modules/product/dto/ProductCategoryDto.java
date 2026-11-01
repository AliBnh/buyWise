package com.buywise.modules.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductCategoryDto {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private String iconName;
}
