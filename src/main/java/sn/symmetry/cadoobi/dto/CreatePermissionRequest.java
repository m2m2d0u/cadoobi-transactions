package sn.symmetry.cadoobi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request payload for creating a new permission")
public class CreatePermissionRequest {

    @Schema(description = "Unique permission code (format: resource:action)", example = "payment:create", required = true)
    @NotBlank(message = "Permission code is required")
    @Pattern(regexp = "^[a-z_]+:[a-z_]+$", message = "Code must follow format: resource:action (lowercase with underscores)")
    @Size(max = 100, message = "Code must not exceed 100 characters")
    private String code;

    @Schema(description = "Human-readable permission name", example = "Create Payment", required = true)
    @NotBlank(message = "Permission name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @Schema(description = "Description of what this permission allows", example = "Allows creating new payment transactions")
    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;

    @Schema(description = "Resource category", example = "payment", required = true)
    @NotBlank(message = "Resource is required")
    @Size(max = 50, message = "Resource must not exceed 50 characters")
    private String resource;

    @Schema(description = "Action type", example = "create", required = true)
    @NotBlank(message = "Action is required")
    @Size(max = 50, message = "Action must not exceed 50 characters")
    private String action;

    @Schema(description = "Whether permission is active", example = "true", defaultValue = "true")
    @Builder.Default
    private Boolean isActive = true;
}
