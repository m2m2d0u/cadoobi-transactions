package sn.symmetry.cadoobi.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Permission entity representing a specific system permission/capability.
 * Examples: "payment:create", "merchant:update", "operator:delete"
 */
@Entity
@Table(name = "permissions", indexes = {
    @Index(name = "idx_permission_code", columnList = "code", unique = true)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Unique permission code (e.g., "payment:create", "merchant:read")
     */
    @Column(nullable = false, unique = true, length = 100)
    private String code;

    /**
     * Human-readable permission name
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * Description of what this permission allows
     */
    @Column(length = 255)
    private String description;

    /**
     * Resource category (e.g., "payment", "merchant", "operator")
     */
    @Column(length = 50)
    private String resource;

    /**
     * Action type (e.g., "create", "read", "update", "delete")
     */
    @Column(length = 50)
    private String action;

    /**
     * Whether this permission is active
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;
}
