package com.buywise.modules.auth.repository;

import com.buywise.modules.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Persistence repository for User entity.
 * 
 * Demonstrates:
 * - Repository Pattern: Hides low-level relational SQL/JPA queries behind a domain-oriented abstraction.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
