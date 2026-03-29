package sn.symmetry.cadoobi.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import sn.symmetry.cadoobi.domain.enums.UserStatus;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * User entity representing a system user account
 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_user_email", columnList = "email", unique = true),
    @Index(name = "idx_user_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * User's email address (unique, used for authentication)
     */
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    /**
     * User's encrypted password
     */
    @Column(nullable = false, length = 255)
    private String passwordHash;

    /**
     * User's full name
     */
    @Column(nullable = false, length = 150)
    private String fullName;

    /**
     * User's phone number (optional)
     */
    @Column(length = 20)
    private String phone;

    /**
     * Account status
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private UserStatus status = UserStatus.PENDING;

    /**
     * Whether the email has been verified
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean emailVerified = false;

    /**
     * Number of failed login attempts
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer failedLoginAttempts = 0;

    /**
     * Password reset token
     */
    @Column(length = 255)
    private String resetToken;

    /**
     * Password reset token expiry
     */
    private Instant resetTokenExpiresAt;

    /**
     * Last successful login timestamp
     */
    private Instant lastLoginAt;

    /**
     * Last password change timestamp
     */
    private Instant passwordChangedAt;

    /**
     * Roles assigned to this user
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id"),
        indexes = {
            @Index(name = "idx_user_roles_user", columnList = "user_id"),
            @Index(name = "idx_user_roles_role", columnList = "role_id")
        }
    )
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Role> roles = new HashSet<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    /**
     * Helper method to check if user has a specific role
     */
    public boolean hasRole(String roleCode) {
        return roles.stream()
            .anyMatch(role -> role.getCode().equals(roleCode));
    }

    /**
     * Helper method to check if user has a specific permission
     */
    public boolean hasPermission(String permissionCode) {
        return roles.stream()
            .flatMap(role -> role.getPermissions().stream())
            .anyMatch(permission -> permission.getCode().equals(permissionCode));
    }
}
