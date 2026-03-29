package sn.symmetry.cadoobi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Payment operator configuration details")
public class OperatorResponse {

    @Schema(description = "Unique operator identifier", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @Schema(description = "Operator code", example = "ORANGE_MONEY")
    private String code;

    @Schema(description = "Operator display name", example = "Orange Money Senegal")
    private String name;

    @Schema(description = "Country code (ISO 3166-1 alpha-2)", example = "SN")
    private String country;

    @Schema(description = "Supports incoming payments", example = "true")
    private Boolean supportsPayin;

    @Schema(description = "Supports outgoing payments", example = "true")
    private Boolean supportsPayout;

    @Schema(description = "API base URL", example = "https://api.orange.sn/v1")
    private String apiBaseUrl;

    @Schema(description = "Whether operator is active", example = "true")
    private Boolean isActive;

    @Schema(description = "Creation timestamp", example = "2024-03-26T10:30:00Z")
    private Instant createdAt;
}
