package sn.symmetry.cadoobi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import sn.symmetry.cadoobi.domain.enums.PayoutStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@Schema(description = "Payout transaction details")
public class PayoutResponse {

    @Schema(description = "Payout transaction ID")
    private UUID id;

    @Schema(description = "Symmetry merchant ID")
    private String merchantId;

    @Schema(description = "Merchant code")
    private String merchantCode;

    @Schema(description = "Operator code used for this payout")
    private String operatorCode;

    @Schema(description = "Recipient mobile number")
    private String recipientNumber;

    @Schema(description = "Declared payout amount")
    private BigDecimal amount;

    @Schema(description = "Operator fee deducted")
    private BigDecimal feeAmount;

    @Schema(description = "Net amount received by recipient")
    private BigDecimal netAmount;

    @Schema(description = "Cadoobi merchant fee charged")
    private BigDecimal merchantFeeAmount;

    @Schema(description = "Currency code")
    private String currency;

    @Schema(description = "Current payout status")
    private PayoutStatus status;

    @Schema(description = "Idempotency key")
    private String idempotencyKey;

    @Schema(description = "Operator's own transaction reference")
    private String operatorTransactionId;

    @Schema(description = "When this payout was created")
    private Instant createdAt;

    @Schema(description = "When this payout was last updated")
    private Instant updatedAt;
}
