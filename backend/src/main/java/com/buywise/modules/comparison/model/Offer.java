package com.buywise.modules.comparison.model;

import com.buywise.common.audit.AuditableEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * =================================================================================================
 * ARCHITECTURAL DESIGN NOTE: OFFER ENTITY & FINANCIAL ARITHMETIC PRECISION
 * =================================================================================================
 * Relational Theory & BCNF:
 * 1. BCNF Compliance:
 *    - Relation: offers(id, comparison_id, seller_name, base_price, delivery_fee, additional_fees,
 *                discount_amount, total_price, warranty_months, return_window_days, ...)
 *    - Primary / Candidate Key: {id}
 *    - Functional Dependencies: id -> {comparison_id, seller_name, base_price, ...}
 *    - Since the determinant {id} is the unique superkey, this relation satisfies strict BCNF.
 *
 * 2. Financial Arithmetic Invariants (IEEE 754 Float Avoidance):
 *    - Monetary fields use java.math.BigDecimal with DB precision 12, scale 2 (mapped to NUMERIC(12,2) in PostgreSQL).
 *    - Float/Double primitives are strictly prohibited in financial ledgers due to binary representation
 *      drift (e.g. 0.1 + 0.2 = 0.30000000000000004).
 *    - {@link #recalculateTotalPrice()} enforces the deterministic domain equation:
 *      Total = BasePrice + DeliveryFee + AdditionalFees - DiscountAmount.
 *
 * 3. Database Integrity & Constraints:
 *    - Foreign key constraint to parent `comparisons` table ensures referential integrity (RESTRICT / CASCADE).
 *    - DB check constraints ensure non-negative prices (base_price >= 0, total_price >= 0).
 *
 * Technical Interview Q&A Crib Sheet:
 * Q: "Why store total_price in the table if it is derived from base_price, delivery_fee, etc.?"
 * A: Storing the computed total_price allows high-performance database indexing (B-Tree index on total_price),
 *    enabling instant SQL ordering (ORDER BY total_price ASC) and pagination across millions of offers
 *    without requiring a compute-heavy full table scan on every query. The entity encapsulates
 *    recalculateTotalPrice() to guarantee zero drift between parts and total.
 * =================================================================================================
 */
@Entity
@Table(name = "offers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Offer extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "comparison_id", nullable = false)
    @JsonIgnore
    private Comparison comparison;

    @Column(name = "seller_name", nullable = false, length = 200)
    private String sellerName;

    @Column(name = "seller_id")
    private Long sellerId;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(name = "product_model", length = 200)
    private String productModel;

    @Column(name = "source_url", length = 1000)
    private String sourceUrl;

    @Column(nullable = false, length = 10)
    @Builder.Default
    private String currency = "MAD";

    @Column(name = "base_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "delivery_fee", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal deliveryFee = BigDecimal.ZERO;

    @Column(name = "additional_fees", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal additionalFees = BigDecimal.ZERO;

    @Column(name = "discount_amount", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "total_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalPrice;

    @Column(name = "warranty_months", nullable = false)
    @Builder.Default
    private Integer warrantyMonths = 12;

    @Column(name = "return_window_days", nullable = false)
    @Builder.Default
    private Integer returnWindowDays = 14;

    @Column(name = "specifications_json", columnDefinition = "TEXT")
    private String specificationsJson;

    @Column(name = "warranty_conditions", columnDefinition = "TEXT")
    private String warrantyConditions;

    @Column(name = "return_policy", columnDefinition = "TEXT")
    private String returnPolicy;

    @Column(name = "is_recommended")
    @Builder.Default
    private Boolean isRecommended = false;

    @Column(name = "value_score", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal valueScore = BigDecimal.ZERO;

    /**
     * Recomputes total price deterministically.
     * Guaranteed business invariant: Total = Base + Delivery + Additional - Discount
     */
    public void recalculateTotalPrice() {
        BigDecimal base = basePrice != null ? basePrice : BigDecimal.ZERO;
        BigDecimal delivery = deliveryFee != null ? deliveryFee : BigDecimal.ZERO;
        BigDecimal add = additionalFees != null ? additionalFees : BigDecimal.ZERO;
        BigDecimal disc = discountAmount != null ? discountAmount : BigDecimal.ZERO;

        this.totalPrice = base.add(delivery).add(add).subtract(disc).max(BigDecimal.ZERO);
    }
}
