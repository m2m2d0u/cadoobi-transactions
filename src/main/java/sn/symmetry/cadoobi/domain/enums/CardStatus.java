package sn.symmetry.cadoobi.domain.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Gift card status")
public enum CardStatus {
    @Schema(description = "Card is active and has full balance available")
    ACTIVE,

    @Schema(description = "Card has been partially redeemed")
    PARTIALLY_USED,

    @Schema(description = "Card balance has been fully redeemed")
    FULLY_USED,

    @Schema(description = "Card has expired")
    EXPIRED,

    @Schema(description = "Card has been blocked and cannot be used")
    BLOCKED
}
