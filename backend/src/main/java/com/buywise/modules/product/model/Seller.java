package com.buywise.modules.product.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Entity tracking merchant credibility, verified vendor status, and default return policies.
 * 
 * Demonstrates:
 * - Boyce-Codd Normal Form (BCNF): Normalizes vendor metadata so merchant reputation scores
 *   are stored once and linked across thousands of offers rather than duplicated.
 * - Exact Numeric Representation: Uses BigDecimal for reputationScore to preserve precise rating averages.
 */
@Entity
@Table(name = "sellers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Seller {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "website_url", length = 500)
    private String websiteUrl;

    @Column(name = "reputation_score", precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal reputationScore = new BigDecimal("4.00");

    @Column(name = "return_policy_summary", columnDefinition = "TEXT")
    private String returnPolicySummary;

    @Column(name = "is_verified")
    @Builder.Default
    private Boolean isVerified = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
