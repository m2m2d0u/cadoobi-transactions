package sn.symmetry.cadoobi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request to create a new webhook configuration")
public class CreateWebhookConfigurationRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    @Schema(description = "Webhook name for identification", example = "Payment Notifications", required = true)
    private String name;

    @NotBlank(message = "URL is required")
    @Size(max = 500, message = "URL must not exceed 500 characters")
    @Pattern(regexp = "^https?://.*", message = "URL must start with http:// or https://")
    @Schema(description = "Webhook callback URL", example = "https://api.example.com/webhooks/cadoobi", required = true)
    private String url;

    @Size(max = 255, message = "Description must not exceed 255 characters")
    @Schema(description = "Optional description of the webhook", example = "Receives payment event notifications")
    private String description;

    @Schema(description = "List of event types to subscribe to. If empty, subscribes to all events.",
            example = "[\"payment.created\", \"payment.completed\", \"payout.created\", \"payout.completed\"]")
    private List<String> subscribedEvents;

    @Min(value = 0, message = "Max retries must be at least 0")
    @Max(value = 10, message = "Max retries cannot exceed 10")
    @Schema(description = "Maximum number of retry attempts for failed deliveries", example = "3", defaultValue = "3")
    private Integer maxRetries;

    @Min(value = 5, message = "Timeout must be at least 5 seconds")
    @Max(value = 120, message = "Timeout cannot exceed 120 seconds")
    @Schema(description = "Timeout in seconds for webhook HTTP requests", example = "30", defaultValue = "30")
    private Integer timeoutSeconds;
}
