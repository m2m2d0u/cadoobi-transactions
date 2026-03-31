package sn.symmetry.cadoobi.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "merchant_accounts", indexes = {
    @Index(name = "idx_merchant_account_merchant", columnList = "merchant_id")
},
uniqueConstraints = {
    @UniqueConstraint(name = "uq_merchant_account", columnNames = {"merchant_id", "currency"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MerchantAccount extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchant;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "XOF";

    @Column(name = "balance", nullable = false, precision = 15, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "locked_balance", nullable = false, precision = 15, scale = 2)
    private BigDecimal lockedBalance = BigDecimal.ZERO;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    public BigDecimal getAvailableBalance() {
        return balance.subtract(lockedBalance);
    }
}
