package com.buywise.modules.purchase.repository;

import com.buywise.modules.purchase.model.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Long> {

    List<Purchase> findByUserIdOrderByPurchaseDateDesc(Long userId);

    Optional<Purchase> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT p FROM Purchase p WHERE p.userId = :userId AND p.warrantyExpirationDate BETWEEN :startDate AND :endDate ORDER BY p.warrantyExpirationDate ASC")
    List<Purchase> findExpiringWarranties(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    long countByUserId(Long userId);

    @Query("SELECT COALESCE(SUM(p.pricePaid), 0) FROM Purchase p WHERE p.userId = :userId")
    BigDecimal sumTotalSpentByUserId(@Param("userId") Long userId);
}
