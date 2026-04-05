package sn.symmetry.cadoobi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@Schema(description = "System parameter configuration")
public class ParameterResponse {

    @Schema(description = "Parameter ID")
    private UUID id;

    @Schema(description = "Parameter key", example = "payment.timeout.seconds")
    private String key;

    @Schema(description = "Parameter value", example = "120")
    private String value;

    @Schema(description = "Parameter category", example = "PAYMENT")
    private String category;

    @Schema(description = "Parameter description")
    private String description;

    @Schema(description = "Whether the parameter is active")
    private Boolean isActive;

    @Schema(description = "Whether the parameter is a system parameter (cannot be deleted)")
    private Boolean isSystem;

    @Schema(description = "Creation timestamp")
    private Instant createdAt;

    @Schema(description = "Last update timestamp")
    private Instant updatedAt;
}
