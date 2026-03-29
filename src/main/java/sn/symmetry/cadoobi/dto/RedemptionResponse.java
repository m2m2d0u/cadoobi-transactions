package sn.symmetry.cadoobi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import sn.symmetry.cadoobi.domain.enums.RedemptionStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Gift card redemption transaction details")
public class RedemptionResponse {

    @Schema(description = "Unique redemption transaction identifier", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @Schema(description = "Gift card identifier", example = "987e6543-e21b-98d7-a654-123456789abc")
    private UUID giftCardId;

    @Schema(description = "Merchant identifier", example = "MCH-001")
    private String merchantId;

    @Schema(description = "Idempotency key for this redemption", example = "REDEEM-20240326-ABC123")
    private String idempotencyKey;

    @Schema(description = "Amount redeemed from the card", example = "5000")
    private BigDecimal amountRedeemed;

    @Schema(description = "Remaining balance after redemption", example = "30000")
    private BigDecimal remainingBalance;

    @Schema(description = "Redemption status", example = "COMPLETED")
    private RedemptionStatus status;

    @Schema(description = "Timestamp when redemption was completed", example = "2024-03-26T10:30:00Z")
    private Instant redeemedAt;

    @Schema(description = "Timestamp when redemption was created", example = "2024-03-26T10:30:00Z")
    private Instant createdAt;
}
