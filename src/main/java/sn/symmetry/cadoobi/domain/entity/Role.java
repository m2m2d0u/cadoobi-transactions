package sn.symmetry.cadoobi.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Role entity representing a collection of permissions.
 * Examples: "ADMIN", "MERCHANT_MANAGER", "FINANCE_OFFICER"
 */
@Entity
@Table(name = "roles", indexes = {
    @Index(name = "idx_role_code", columnList = "code", unique = true)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Unique role code (e.g., "ADMIN", "MERCHANT_MANAGER")
     */
    @Column(nullable = false, unique = true, length = 50)
    private String code;

    /**
     * Human-readable role name
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * Description of this role
     */
    @Column(length = 255)
    private String description;

    /**
     * Whether this role is active
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    /**
     * Whether this is a system role (cannot be deleted)
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean isSystemRole = false;

    /**
     * Permissions granted to this role
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "role_permissions",
        joinColumns = @JoinColumn(name = "role_id"),
        inverseJoinColumns = @JoinColumn(name = "permission_id"),
        indexes = {
            @Index(name = "idx_role_permissions_role", columnList = "role_id"),
            @Index(name = "idx_role_permissions_permission", columnList = "permission_id")
        }
    )
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Permission> permissions = new HashSet<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;
}
