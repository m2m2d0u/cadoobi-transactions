package sn.symmetry.cadoobi.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * API Key entity for authenticating external API requests.
 * Each API key is associated with a user and can have referrer restrictions.
 */
@Entity
@Table(name = "api_keys",
    indexes = {
        @Index(name = "idx_api_key_token", columnList = "api_key", unique = true),
        @Index(name = "idx_api_key_user", columnList = "user_id"),
        @Index(name = "idx_api_key_active", columnList = "is_active")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiKey extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;

    @Column(name = "api_key", nullable = false, unique = true, length = 512)
    private String apiKey;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 255)
    private String description;

    /**
     * Comma-separated list of allowed referrer domains/URLs.
     * If null or empty, no referrer restrictions apply.
     * Example: "https://example.com,https://app.example.com"
     */
    @Column(name = "allowed_referrers", columnDefinition = "TEXT")
    private String allowedReferrers;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "last_used_at")
    private Instant lastUsedAt;
}
