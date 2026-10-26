package com.buywise.modules.purchase.model;

import com.buywise.common.audit.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Purchase aggregate entity representing a registered consumer ownership asset.
 * 
 * Demonstrates:
 * - Deterministic Invariants: Calculates warranty expiration date deterministically from purchase date + months.
 * - Resource Ownership: Partitioned strictly by userId to guarantee multi-tenant consumer privacy.
 */
@Entity
@Table(name = "purchases")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Purchase extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "product_name", nullable = false, length = 255)
    private String productName;

    @Column(name = "model_number", length = 100)
    private String modelNumber;

    @Column(length = 100)
    private String category;

    @Column(name = "seller_name", nullable = false, length = 200)
    private String sellerName;

    @Column(name = "purchase_date", nullable = false)
    private LocalDate purchaseDate;

    @Column(name = "price_paid", nullable = false, precision = 12, scale = 2)
    private BigDecimal pricePaid;

    @Column(nullable = false, length = 10)
    @Builder.Default
    private String currency = "MAD";

    @Column(name = "warranty_months", nullable = false)
    @Builder.Default
    private Integer warrantyMonths = 12;

    @Column(name = "warranty_expiration_date", nullable = false)
    private LocalDate warrantyExpirationDate;

    @Column(name = "return_deadline")
    private LocalDate returnDeadline;

    @Column(name = "serial_number", length = 100)
    private String serialNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @Builder.Default
    private PurchaseStatus status = PurchaseStatus.ACTIVE;

    @Column(columnDefinition = "TEXT")
    private String notes;

    /**
     * Recomputes warranty expiration date based on purchase date and warranty duration.
     */
    public void calculateExpirationDate() {
        if (this.purchaseDate != null && this.warrantyMonths != null) {
            this.warrantyExpirationDate = this.purchaseDate.plusMonths(this.warrantyMonths);
        }
    }
}
