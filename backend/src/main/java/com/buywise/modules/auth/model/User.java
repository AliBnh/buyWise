package com.buywise.modules.auth.model;

import com.buywise.common.audit.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * User domain entity representing an authenticated BuyWise consumer or administrator.
 * 
 * Demonstrates:
 * - Encapsulation: Private state manipulated through clean accessors and constructors.
 * - Inheritance: Extends {@link AuditableEntity} for automatic created_at and updated_at tracking.
 * - Defensive Design: Unique constraint on email and fallback defaults for preferences.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @Builder.Default
    private Role role = Role.ROLE_USER;

    @Column(name = "preferred_currency", nullable = false, length = 10)
    @Builder.Default
    private String preferredCurrency = "MAD";
}
