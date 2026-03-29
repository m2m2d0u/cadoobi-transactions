package sn.symmetry.cadoobi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Permission details")
public class PermissionResponse {

    @Schema(description = "Unique permission identifier", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @Schema(description = "Permission code", example = "payment:create")
    private String code;

    @Schema(description = "Permission name", example = "Create Payment")
    private String name;

    @Schema(description = "Permission description", example = "Allows creating new payment transactions")
    private String description;

    @Schema(description = "Resource category", example = "payment")
    private String resource;

    @Schema(description = "Action type", example = "create")
    private String action;

    @Schema(description = "Whether permission is active", example = "true")
    private Boolean isActive;

    @Schema(description = "Creation timestamp", example = "2024-03-26T10:30:00Z")
    private Instant createdAt;

    @Schema(description = "Last update timestamp", example = "2024-03-26T10:30:00Z")
    private Instant updatedAt;
}
