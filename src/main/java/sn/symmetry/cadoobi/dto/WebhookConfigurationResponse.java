package sn.symmetry.cadoobi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Webhook configuration information")
public class WebhookConfigurationResponse {

    @Schema(description = "Unique identifier")
    private UUID id;

    @Schema(description = "Webhook name", example = "Payment Notifications")
    private String name;

    @Schema(description = "Webhook callback URL", example = "https://api.example.com/webhooks/cadoobi")
    private String url;

    @Schema(description = "Webhook description", example = "Receives payment event notifications")
    private String description;

    @Schema(description = "Webhook secret for signature verification (only shown once during creation)")
    private String secret;

    @Schema(description = "Masked webhook secret for display (e.g., 'whsec_****...****abcd')")
    private String maskedSecret;

    @Schema(description = "List of subscribed event types",
            example = "[\"payment.created\", \"payment.completed\", \"payout.created\", \"payout.completed\"]")
    private List<String> subscribedEvents;

    @Schema(description = "Whether the webhook is active", example = "true")
    private Boolean isActive;

    @Schema(description = "Last time the webhook was triggered", example = "2024-12-15T10:30:00Z")
    private Instant lastTriggeredAt;

    @Schema(description = "Maximum number of retry attempts", example = "3")
    private Integer maxRetries;

    @Schema(description = "Timeout in seconds", example = "30")
    private Integer timeoutSeconds;

    @Schema(description = "Creation timestamp")
    private Instant createdAt;

    @Schema(description = "Last update timestamp")
    private Instant updatedAt;
}
