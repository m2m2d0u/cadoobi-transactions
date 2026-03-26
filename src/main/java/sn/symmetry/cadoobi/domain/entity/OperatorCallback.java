package sn.symmetry.cadoobi.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "operator_callbacks", indexes = {
    @Index(name = "idx_callback_payment", columnList = "payment_transaction_id"),
    @Index(name = "idx_callback_operator", columnList = "operator_id"),
    @Index(name = "idx_callback_reference", columnList = "operator_reference"),
    @Index(name = "idx_callback_processed", columnList = "processed_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OperatorCallback extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_transaction_id")
    private PaymentTransaction paymentTransaction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operator_id", nullable = false)
    private Operator operator;

    @Column(name = "operator_reference", nullable = false, unique = true, length = 100)
    private String operatorReference;

    @Column(name = "raw_payload", nullable = false, columnDefinition = "TEXT")
    private String rawPayload;

    @Column(name = "operator_status", length = 50)
    private String operatorStatus;

    @Column(name = "processed_at")
    private Instant processedAt;
}
