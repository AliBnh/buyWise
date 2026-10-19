package com.buywise.modules.comparison.model;

import com.buywise.common.audit.AuditableEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Child entity representing a specific seller offer within a comparison.
 * 
 * Demonstrates:
 * - Deterministic Invariants: {@link #recalculateTotalPrice()} ensures total price always equals
 *   (basePrice + deliveryFee + additionalFees - discountAmount) without depending on UI or external systems.
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
