package sn.symmetry.cadoobi.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sn.symmetry.cadoobi.domain.enums.UserStatus;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Authentication response containing the JWT token and user info")
public class AuthResponse {

    @Schema(description = "Bearer JWT token", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String token;

    @Schema(description = "Refresh JWT token", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String refreshToken;

    @Schema(description = "Token type", example = "Bearer")
    @Builder.Default
    private String tokenType = "Bearer";

    @Schema(description = "Token expiration in milliseconds", example = "86400000")
    private long expiresIn;

    @Schema(description = "Authenticated user ID")
    private UUID userId;

    @Schema(description = "User email", example = "admin@example.com")
    private String email;

    @Schema(description = "User full name", example = "Amadou Diallo")
    private String fullName;

    @Schema(description = "Account status", example = "ACTIVE")
    private UserStatus status;

    @Schema(description = "List of role codes assigned to the user", example = "[\"ADMIN\", \"MERCHANT_MANAGER\"]")
    private List<String> roles;

    @Schema(description = "List of permission codes granted to the user", example = "[\"payment:create\", \"merchant:read\"]")
    private List<String> permissions;
}
