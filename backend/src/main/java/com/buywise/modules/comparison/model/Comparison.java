package com.buywise.modules.comparison.model;

import com.buywise.common.audit.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Root aggregate entity representing a multi-offer purchase comparison.
 * 
 * Demonstrates:
 * - Aggregate Root (DDD): Manages lifecycle and cascade operations for associated {@link Offer} child entities.
 * - Encapsulation: Offers are modified through aggregate methods.
 * - User Isolation: Partitioned by `userId`.
 */
@Entity
@Table(name = "comparisons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comparison extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(name = "category_name", nullable = false, length = 100)
    private String categoryName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @Builder.Default
    private ComparisonStatus status = ComparisonStatus.DRAFT;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "ai_analysis_json", columnDefinition = "TEXT")
    private String aiAnalysisJson;

    @Column(name = "ai_analyzed_at")
    private Instant aiAnalyzedAt;

    @OneToMany(mappedBy = "comparison", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Offer> offers = new ArrayList<>();

    public void addOffer(Offer offer) {
        offers.add(offer);
        offer.setComparison(this);
    }

    public void removeOffer(Offer offer) {
        offers.remove(offer);
        offer.setComparison(null);
    }
}
