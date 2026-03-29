package sn.symmetry.cadoobi.domain.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Merchant account status")
public enum MerchantStatus {
    @Schema(description = "Merchant account is pending approval")
    PENDING,

    @Schema(description = "Merchant account is active and operational")
    ACTIVE,

    @Schema(description = "Merchant account is temporarily suspended")
    SUSPENDED,

    @Schema(description = "Merchant account is blocked")
    BLOCKED
}
