package sn.symmetry.cadoobi.domain.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Type of payment operation")
public enum OperationType {
    @Schema(description = "Incoming payment (money in)")
    PAYIN,

    @Schema(description = "Outgoing payment (money out)")
    PAYOUT
}
