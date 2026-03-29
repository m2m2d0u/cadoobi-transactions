package sn.symmetry.cadoobi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request payload for creating a new payment operator")
public class CreateOperatorRequest {

    @Schema(description = "Unique operator code identifier", example = "ORANGE_MONEY", required = true)
    @NotBlank(message = "Code is required")
    @Size(max = 30, message = "Code must not exceed 30 characters")
    private String code;

    @Schema(description = "Operator display name", example = "Orange Money Senegal", required = true)
    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @Schema(description = "Country code (ISO 3166-1 alpha-2)", example = "SN", required = true)
    @NotBlank(message = "Country is required")
    @Size(min = 2, max = 2, message = "Country must be exactly 2 characters")
    private String country;

    @Schema(description = "Whether operator supports incoming payments", example = "true", defaultValue = "true")
    private Boolean supportsPayin = true;

    @Schema(description = "Whether operator supports outgoing payments", example = "true", defaultValue = "true")
    private Boolean supportsPayout = true;

    @Schema(description = "Base URL for operator API integration", example = "https://api.orange.sn/v1")
    @Size(max = 500, message = "API base URL must not exceed 500 characters")
    private String apiBaseUrl;

    @Schema(description = "Whether operator is active and available for transactions", example = "true", defaultValue = "true")
    private Boolean isActive = true;
}
