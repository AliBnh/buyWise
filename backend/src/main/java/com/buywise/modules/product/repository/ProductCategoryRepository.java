package com.buywise.modules.product.repository;

import com.buywise.modules.product.model.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {

    Optional<ProductCategory> findBySlug(String slug);

    Optional<ProductCategory> findByNameIgnoreCase(String name);
}
