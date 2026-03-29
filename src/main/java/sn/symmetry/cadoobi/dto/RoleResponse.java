package sn.symmetry.cadoobi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Role details with associated permissions")
public class RoleResponse {

    @Schema(description = "Unique role identifier", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @Schema(description = "Role code", example = "ADMIN")
    private String code;

    @Schema(description = "Role name", example = "Administrator")
    private String name;

    @Schema(description = "Role description", example = "Full system access")
    private String description;

    @Schema(description = "Whether role is active", example = "true")
    private Boolean isActive;

    @Schema(description = "Whether role is a system role (cannot be deleted)", example = "true")
    private Boolean isSystemRole;

    @Schema(description = "Permissions granted to this role")
    private List<PermissionResponse> permissions;

    @Schema(description = "Number of users with this role", example = "5")
    private Integer userCount;

    @Schema(description = "Creation timestamp", example = "2024-03-26T10:30:00Z")
    private Instant createdAt;

    @Schema(description = "Last update timestamp", example = "2024-03-26T10:30:00Z")
    private Instant updatedAt;
}
