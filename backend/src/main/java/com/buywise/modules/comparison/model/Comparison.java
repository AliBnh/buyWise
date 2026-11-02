package com.buywise.modules.comparison.model;

import com.buywise.common.audit.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * =================================================================================================
 * ARCHITECTURAL DESIGN NOTE: BOYCE-CODD NORMAL FORM (BCNF) & AGGREGATE ROOT PATTERN
 * =================================================================================================
 * Relational Theory & Boyce-Codd Normal Form (BCNF):
 * 1. Formal BCNF Condition:
 *    A relation schema R is in BCNF with respect to a set of functional dependencies F if,
 *    for every non-trivial functional dependency (X -> Y) in F+, X is a superkey of R.
 *
 * 2. Normalization Progression in BuyWise:
 *    - 1NF: All attributes are atomic. Offers are NOT stored as delimited strings or JSON blobs
 *      inside comparisons; each offer is decomposed into its own relation.
 *    - 2NF: No partial key dependencies. All non-prime attributes fully depend on primary key `id`.
 *    - 3NF: No transitive dependencies. Comparisons do not store seller ratings or product catalog
 *      details; those reside in dedicated tables.
 *    - BCNF: Stricter than 3NF. In this schema, the only candidate key is {id}. Every determinant
 *      X of any functional dependency (id -> {userId, title, categoryName, status, notes})
 *      is a true superkey. This eliminates all update, insertion, and deletion anomalies.
 *
 * 3. Anomaly Elimination Explained:
 *    - Insertion Anomaly: A user can create a comparison draft before collecting offers.
 *    - Deletion Anomaly: Deleting an offer from a comparison does not delete the parent comparison
 *      or the seller record.
 *    - Update Anomaly: Updating comparison metadata (e.g. title or status) modifies exactly one
 *      tuple, avoiding duplicate data synchronization across multiple offer rows.
 *
 * Domain-Driven Design (DDD) - Aggregate Root:
 *    - Comparison acts as the Aggregate Root managing the lifecycle of child {@link Offer} entities.
 *    - Cascading operations (`CascadeType.ALL`, `orphanRemoval = true`) guarantee that offers cannot
 *      exist as orphaned records outside an active comparison boundary.
 *    - Bidirectional relationship consistency is enforced through domain helper methods
 *      {@link #addOffer(Offer)} and {@link #removeOffer(Offer)}.
 *
 * Relational Normalization Deep Dive:
 * Q: "What is the exact theoretical difference between 3NF and BCNF?"
 * A: 3NF permits a dependency X -> Y where X is not a superkey IF Y is a prime attribute (part of a candidate key).
 *    BCNF removes this exception entirely: EVERY determinant X in any non-trivial dependency X -> Y
 *    MUST be a superkey. In BCNF, there are zero non-trivial dependencies with non-superkey determinants.
 * =================================================================================================
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
