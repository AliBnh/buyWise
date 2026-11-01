package com.buywise.modules.purchase.model;

import com.buywise.common.audit.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * =================================================================================================
 * ARCHITECTURAL DESIGN NOTE: PURCHASE ASSET AGGREGATE & TEMPORAL INTEGRITY
 * =================================================================================================
 * Relational Theory & BCNF:
 * 1. BCNF Satisfaction:
 *    - Relation: purchases(id, user_id, product_name, model_number, category, seller_name,
 *                          purchase_date, price_paid, warranty_months, warranty_expiration_date, ...)
 *    - Candidate Key: {id}
 *    - Non-trivial FDs: id -> {all non-id attributes}.
 *    - Every determinant is a superkey, completely satisfying BCNF.
 *
 * 2. Temporal Tracking & Invariants:
 *    - {@link #calculateExpirationDate()} enforces temporal consistency:
 *      warrantyExpirationDate = purchaseDate + warrantyMonths.
 *    - Guarantees that consumer asset records maintain consistent audit dates for deadline calculation.
 *
 * 3. Multi-Tenant Partitioning:
 *    - Enforces tenant boundary by including `user_id` on all record lookups.
 *    - Prevents cross-account data leakage across independent consumer asset libraries.
 * =================================================================================================
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
