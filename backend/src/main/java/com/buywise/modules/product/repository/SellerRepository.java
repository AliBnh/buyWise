package com.buywise.modules.product.repository;

import com.buywise.modules.product.model.Seller;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SellerRepository extends JpaRepository<Seller, Long> {

    Optional<Seller> findByNameIgnoreCase(String name);
}
