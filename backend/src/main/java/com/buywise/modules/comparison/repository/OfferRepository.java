package com.buywise.modules.comparison.repository;

import com.buywise.modules.comparison.model.Offer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OfferRepository extends JpaRepository<Offer, Long> {

    List<Offer> findByComparisonIdOrderByTotalPriceAsc(Long comparisonId);

    Optional<Offer> findByIdAndComparisonId(Long id, Long comparisonId);
}
