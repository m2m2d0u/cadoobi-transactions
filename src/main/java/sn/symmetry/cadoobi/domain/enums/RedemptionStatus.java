package sn.symmetry.cadoobi.domain.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Gift card redemption transaction status")
public enum RedemptionStatus {
    @Schema(description = "Redemption is pending processing")
    PENDING,

    @Schema(description = "Redemption completed successfully")
    COMPLETED,

    @Schema(description = "Redemption failed")
    FAILED
}
