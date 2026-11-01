package com.buywise.modules.auth.model;

import com.buywise.common.audit.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * =================================================================================================
 * ARCHITECTURAL DESIGN NOTE: USER DOMAIN ENTITY & RELATIONAL INTEGRITY
 * =================================================================================================
 * Relational Theory & Candidate Keys:
 * 1. BCNF Satisfaction:
 *    - Candidate Keys: {id} and {email} (enforced by UNIQUE constraint on email).
 *    - Functional Dependencies:
 *      id -> {email, passwordHash, fullName, role, preferredCurrency, createdAt, updatedAt}
 *      email -> {id, passwordHash, fullName, role, preferredCurrency, createdAt, updatedAt}
 *    - Since every determinant (id, email) is a candidate key (superkey), the relation is in strict BCNF!
 *
 * 2. Security & Defense-in-Depth:
 *    - Passwords are NEVER stored in plaintext; only BCrypt hashes (passwordHash) are persisted.
 *    - Implements AuditableEntity for automated temporal auditing of account creation and updates.
 * =================================================================================================
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
