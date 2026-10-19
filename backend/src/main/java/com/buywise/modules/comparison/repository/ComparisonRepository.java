package com.buywise.modules.comparison.repository;

import com.buywise.modules.comparison.model.Comparison;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Persistence repository for Comparison entities with strict user isolation checks.
 */
@Repository
public interface ComparisonRepository extends JpaRepository<Comparison, Long> {

    List<Comparison> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Comparison> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT c FROM Comparison c LEFT JOIN FETCH c.offers WHERE c.id = :id AND c.userId = :userId")
    Optional<Comparison> findByIdAndUserIdWithOffers(@Param("id") Long id, @Param("userId") Long userId);

    boolean existsByIdAndUserId(Long id, Long userId);

    long countByUserId(Long userId);
}
