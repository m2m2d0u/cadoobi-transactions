package sn.symmetry.cadoobi.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import sn.symmetry.cadoobi.domain.enums.RedemptionStatus;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "gift_card_redemptions", indexes = {
    @Index(name = "idx_redemption_card", columnList = "gift_card_id"),
    @Index(name = "idx_redemption_merchant", columnList = "merchant_id"),
    @Index(name = "idx_redemption_status", columnList = "status"),
    @Index(name = "idx_redemption_idempotency", columnList = "idempotency_key")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GiftCardRedemption extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gift_card_id", nullable = false)
    private GiftCard giftCard;

    @Column(name = "merchant_id", nullable = false, length = 36)
    private String merchantId;

    @Column(name = "idempotency_key", nullable = false, unique = true)
    private String idempotencyKey;

    @Column(name = "amount_redeemed", nullable = false, precision = 15, scale = 2)
    private BigDecimal amountRedeemed;

    @Column(name = "remaining_balance", precision = 15, scale = 2)
    private BigDecimal remainingBalance;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RedemptionStatus status = RedemptionStatus.PENDING;

    @Column(name = "redeemed_at")
    private Instant redeemedAt;
}
