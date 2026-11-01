package com.buywise.modules.document.dto;

import com.buywise.modules.document.model.VerificationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FactVerificationRequest {

    @NotNull(message = "Verification status is required")
    private VerificationStatus verificationStatus;

    /**
     * Optional edited or corrected value provided by the user.
     */
    private String verifiedValue;

    /**
     * Whether to automatically update the linked Purchase or Offer with this verified value.
     */
    @Builder.Default
    private Boolean applyToLinkedResource = true;
}
