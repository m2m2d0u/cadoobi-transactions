package sn.symmetry.cadoobi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import sn.symmetry.cadoobi.domain.enums.UserStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "User account details")
public class UserResponse {

    @Schema(description = "Unique user identifier", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @Schema(description = "User email address", example = "user@example.com")
    private String email;

    @Schema(description = "User full name", example = "Amadou Diallo")
    private String fullName;

    @Schema(description = "User phone number", example = "+221771234567")
    private String phone;

    @Schema(description = "Account status", example = "ACTIVE")
    private UserStatus status;

    @Schema(description = "Whether email is verified", example = "true")
    private Boolean emailVerified;

    @Schema(description = "Number of failed login attempts", example = "0")
    private Integer failedLoginAttempts;

    @Schema(description = "Last successful login timestamp", example = "2024-03-26T10:30:00Z")
    private Instant lastLoginAt;

    @Schema(description = "Last password change timestamp", example = "2024-03-26T10:30:00Z")
    private Instant passwordChangedAt;

    @Schema(description = "Roles assigned to this user")
    private List<RoleResponse> roles;

    @Schema(description = "Creation timestamp", example = "2024-03-26T10:30:00Z")
    private Instant createdAt;

    @Schema(description = "Last update timestamp", example = "2024-03-26T10:30:00Z")
    private Instant updatedAt;
}
