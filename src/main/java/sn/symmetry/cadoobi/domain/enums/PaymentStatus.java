package sn.symmetry.cadoobi.domain.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Payment transaction status")
public enum PaymentStatus {
    @Schema(description = "Payment has been initiated but not yet processed")
    INITIATED,

    @Schema(description = "Payment is being processed by the operator")
    PENDING,

    @Schema(description = "Payment has been completed successfully")
    COMPLETED,

    @Schema(description = "Payment processing failed")
    FAILED,

    @Schema(description = "Payment has expired without completion")
    EXPIRED,

    @Schema(description = "Payment was cancelled")
    CANCELLED
}
