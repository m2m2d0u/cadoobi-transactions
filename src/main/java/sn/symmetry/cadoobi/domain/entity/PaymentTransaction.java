package sn.symmetry.cadoobi.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import sn.symmetry.cadoobi.domain.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "payment_transactions", indexes = {
    @Index(name = "idx_payment_reference", columnList = "reference"),
    @Index(name = "idx_payment_status", columnList = "status"),
    @Index(name = "idx_payment_merchant", columnList = "merchant_id"),
    @Index(name = "idx_payment_operator", columnList = "operator_id"),
    @Index(name = "idx_payment_operator_txn", columnList = "operator_transaction_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentTransaction extends BaseEntity {

    @Column(name = "reference", nullable = false, unique = true, length = 100)
    private String reference;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operator_id", nullable = false)
    private Operator operator;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "fee_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal feeAmount = BigDecimal.ZERO;

    @Column(name = "net_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal netAmount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "XOF";

    @Column(name = "payer_phone", nullable = false, length = 20)
    private String payerPhone;

    @Column(name = "payer_full_name", length = 150)
    private String payerFullName;

    @Column(name = "recipient_phone", nullable = false, length = 20)
    private String recipientPhone;

    @Column(name = "recipient_name", length = 150)
    private String recipientName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PaymentStatus status = PaymentStatus.INITIATED;

    @Column(name = "operator_transaction_id", length = 100)
    private String operatorTransactionId;

    @Column(name = "payment_url", length = 1000)
    private String paymentUrl;

    @Column(name = "callback_url", nullable = false, length = 500)
    private String callbackUrl;

    @Column(name = "expires_at")
    private Instant expiresAt;
}
