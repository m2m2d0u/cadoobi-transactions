package sn.symmetry.cadoobi.dto;

import lombok.*;
import sn.symmetry.cadoobi.domain.enums.RedemptionStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RedemptionResponse {

    private UUID id;
    private UUID giftCardId;
    private String merchantId;
    private String idempotencyKey;
    private BigDecimal amountRedeemed;
    private BigDecimal remainingBalance;
    private RedemptionStatus status;
    private Instant redeemedAt;
    private Instant createdAt;
}
