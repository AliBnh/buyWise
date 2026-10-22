package com.buywise.modules.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CitedReferenceDto {

    private String resourceType; // PURCHASE, COMPARISON, DOCUMENT, DEADLINE
    private Long resourceId;
    private String title;
    private String snippet;
}
