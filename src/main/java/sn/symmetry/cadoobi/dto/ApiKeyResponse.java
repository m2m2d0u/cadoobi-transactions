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
@Schema(description = "API key information")
public class ApiKeyResponse {

    @Schema(description = "Unique identifier")
    private UUID id;

    @Schema(description = "API key value (only shown once during creation)")
    private String apiKey;

    @Schema(description = "Masked API key for display (e.g., 'pk_****...****abcd')")
    private String maskedApiKey;

    @Schema(description = "API key name", example = "Production API Key")
    private String name;

    @Schema(description = "API key description", example = "Used for production mobile app")
    private String description;

    @Schema(description = "List of allowed referrer domains/URLs",
            example = "[\"https://example.com\", \"https://app.example.com\"]")
    private List<String> allowedReferrers;

    @Schema(description = "Whether the API key is active", example = "true")
    private Boolean isActive;

    @Schema(description = "Expiration date", example = "2025-12-31T23:59:59Z")
    private Instant expiresAt;

    @Schema(description = "Last time the API key was used", example = "2024-12-15T10:30:00Z")
    private Instant lastUsedAt;

    @Schema(description = "Creation timestamp")
    private Instant createdAt;

    @Schema(description = "Last update timestamp")
    private Instant updatedAt;
}
