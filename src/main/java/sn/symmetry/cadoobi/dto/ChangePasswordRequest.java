package sn.symmetry.cadoobi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request payload for changing user password")
public class ChangePasswordRequest {

    @Schema(description = "Current password", example = "OldP@ss123", required = true)
    @NotBlank(message = "Current password is required")
    private String currentPassword;

    @Schema(description = "New password (min 8 characters)", example = "NewP@ss123", required = true)
    @NotBlank(message = "New password is required")
    @Size(min = 8, max = 100, message = "New password must be between 8 and 100 characters")
    private String newPassword;

    @Schema(description = "Confirm new password", example = "NewP@ss123", required = true)
    @NotBlank(message = "Password confirmation is required")
    private String confirmPassword;
}
