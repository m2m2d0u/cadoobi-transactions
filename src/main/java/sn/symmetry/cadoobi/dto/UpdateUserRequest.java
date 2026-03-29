package sn.symmetry.cadoobi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.*;
import sn.symmetry.cadoobi.domain.enums.UserStatus;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request payload for updating an existing user")
public class UpdateUserRequest {

    @Schema(description = "User email address", example = "user@example.com", required = true)
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @Schema(description = "User full name", example = "Amadou Diallo", required = true)
    @NotBlank(message = "Full name is required")
    @Size(max = 150, message = "Full name must not exceed 150 characters")
    private String fullName;

    @Schema(description = "User phone number", example = "+221771234567")
    @Size(max = 20, message = "Phone must not exceed 20 characters")
    private String phone;

    @Schema(description = "Account status", example = "ACTIVE")
    private UserStatus status;

    @Schema(description = "Role IDs to assign to this user", example = "[\"123e4567-e89b-12d3-a456-426614174000\"]", required = true)
    @NotEmpty(message = "At least one role is required")
    private List<UUID> roleIds;
}
