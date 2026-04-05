package sn.symmetry.cadoobi.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Webhook configuration for sending event notifications to external systems.
 * Each webhook is associated with a user and can subscribe to specific events.
 */
@Entity
@Table(name = "webhook_configurations",
    indexes = {
        @Index(name = "idx_webhook_user", columnList = "user_id"),
        @Index(name = "idx_webhook_active", columnList = "is_active")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebhookConfiguration extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "url", nullable = false, length = 500)
    private String url;

    @Column(name = "description", length = 255)
    private String description;

    /**
     * Secret key used to sign webhook payloads for verification.
     * Should be shared with the client to verify webhook authenticity.
     * Stored encrypted in the database.
     */
    @Column(name = "secret", nullable = false, length = 512)
    private String secret;

    /**
     * Comma-separated list of event types to subscribe to.
     * Examples: "payment.created,payment.completed,payout.created,payout.completed"
     * If null or empty, subscribes to all events.
     */
    @Column(name = "subscribed_events", columnDefinition = "TEXT")
    private String subscribedEvents;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "last_triggered_at")
    private Instant lastTriggeredAt;

    /**
     * Maximum number of retry attempts for failed webhook deliveries
     */
    @Column(name = "max_retries", nullable = false)
    @Builder.Default
    private Integer maxRetries = 3;

    /**
     * Timeout in seconds for webhook HTTP requests
     */
    @Column(name = "timeout_seconds", nullable = false)
    @Builder.Default
    private Integer timeoutSeconds = 30;
}
