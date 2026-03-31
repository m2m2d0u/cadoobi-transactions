package sn.symmetry.cadoobi.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import sn.symmetry.cadoobi.domain.enums.PayoutStatus;

import java.math.BigDecimal;

@Entity
@Table(name = "payout_transactions", indexes = {
    @Index(name = "idx_payout_merchant",    columnList = "merchant_id"),
    @Index(name = "idx_payout_status",      columnList = "status"),
    @Index(name = "idx_payout_operator",    columnList = "operator_id"),
    @Index(name = "idx_payout_idempotency", columnList = "idempotency_key")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayoutTransaction extends BaseEntity {

    @Column(name = "merchant_id", nullable = false, length = 36)
    private String merchantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operator_id", nullable = false)
    private Operator operator;

    @Column(name = "recipient_number", nullable = false)
    private String recipientNumber;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "fee_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal feeAmount = BigDecimal.ZERO;

    @Column(name = "net_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal netAmount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "XOF";

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PayoutStatus status = PayoutStatus.PENDING;

    @Column(name = "idempotency_key", nullable = false, unique = true)
    private String idempotencyKey;

    @Column(name = "operator_transaction_id", length = 100)
    private String operatorTransactionId;

    /** Cadoobi merchant fee charged on top of the declared payout amount. */
    @Column(name = "merchant_fee_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal merchantFeeAmount = BigDecimal.ZERO;
}
