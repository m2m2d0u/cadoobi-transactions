package sn.symmetry.cadoobi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request to update an existing API key")
public class UpdateApiKeyRequest {

    @Size(max = 100, message = "Name must not exceed 100 characters")
    @Schema(description = "API key name for identification", example = "Production API Key")
    private String name;

    @Size(max = 255, message = "Description must not exceed 255 characters")
    @Schema(description = "Optional description of the API key", example = "Used for production mobile app")
    private String description;

    @Schema(description = "List of allowed referrer domains/URLs for security. If empty, no restrictions apply.",
            example = "[\"https://example.com\", \"https://app.example.com\"]")
    private List<String> allowedReferrers;

    @Schema(description = "Whether the API key is active", example = "true")
    private Boolean isActive;

    @Schema(description = "Expiration date for the API key. If null, key never expires.", example = "2025-12-31T23:59:59Z")
    private Instant expiresAt;
}
