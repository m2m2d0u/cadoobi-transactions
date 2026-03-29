package sn.symmetry.cadoobi.domain.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "User account status")
public enum UserStatus {
    @Schema(description = "User account is pending email verification")
    PENDING,

    @Schema(description = "User account is active and operational")
    ACTIVE,

    @Schema(description = "User account is temporarily suspended")
    SUSPENDED,

    @Schema(description = "User account is locked due to failed login attempts")
    LOCKED,

    @Schema(description = "User account is permanently blocked")
    BLOCKED
}
