package sn.symmetry.cadoobi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Request to update a system parameter")
public class UpdateParameterRequest {

    @NotBlank(message = "Value is required")
    @Schema(description = "Parameter value", example = "180", required = true)
    private String value;

    @Schema(description = "Parameter category", example = "PAYMENT")
    private String category;

    @Schema(description = "Parameter description", example = "Payment transaction timeout in seconds")
    private String description;

    @Schema(description = "Whether the parameter is active", example = "true")
    private Boolean isActive;
}
