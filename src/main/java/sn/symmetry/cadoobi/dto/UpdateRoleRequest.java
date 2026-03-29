package sn.symmetry.cadoobi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request payload for updating an existing role")
public class UpdateRoleRequest {

    @Schema(description = "Human-readable role name", example = "Merchant Manager", required = true)
    @NotBlank(message = "Role name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @Schema(description = "Description of this role", example = "Can manage merchant accounts and view transactions")
    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;

    @Schema(description = "Permission IDs to assign to this role", example = "[\"123e4567-e89b-12d3-a456-426614174000\"]", required = true)
    @NotEmpty(message = "At least one permission is required")
    private List<UUID> permissionIds;

    @Schema(description = "Whether role is active", example = "true")
    private Boolean isActive;
}
