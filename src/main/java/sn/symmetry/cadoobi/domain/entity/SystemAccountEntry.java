package sn.symmetry.cadoobi.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import sn.symmetry.cadoobi.domain.enums.LedgerDirection;
import sn.symmetry.cadoobi.domain.enums.SystemEntryType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "system_account_entries", indexes = {
    @Index(name = "idx_sys_entry_account",   columnList = "system_account_id"),
    @Index(name = "idx_sys_entry_type",      columnList = "entry_type"),
    @Index(name = "idx_sys_entry_created",   columnList = "created_at"),
    @Index(name = "idx_sys_entry_payout_tx", columnList = "payout_transaction_id")
})
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemAccountEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "system_account_id", nullable = false, updatable = false)
    private SystemAccount systemAccount;

    @Enumerated(EnumType.STRING)
    @Column(name = "direction", nullable = false, length = 6, updatable = false)
    private LedgerDirection direction;

    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type", nullable = false, length = 30, updatable = false)
    private SystemEntryType entryType;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2, updatable = false)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3, updatable = false)
    private String currency = "XOF";

    @Column(name = "description", length = 255, updatable = false)
    private String description;

    @Column(name = "idempotency_key", nullable = false, unique = true, updatable = false, length = 100)
    private String idempotencyKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payout_transaction_id", updatable = false)
    private PayoutTransaction payoutTransaction;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
