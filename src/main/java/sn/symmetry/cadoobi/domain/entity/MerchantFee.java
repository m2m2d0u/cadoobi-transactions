package sn.symmetry.cadoobi.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import sn.symmetry.cadoobi.domain.enums.FeeType;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "merchant_fees",
    indexes = {
        @Index(name = "idx_merchant_fee_merchant", columnList = "merchant_id"),
        @Index(name = "idx_merchant_fee_active",   columnList = "is_active"),
        @Index(name = "idx_merchant_fee_dates",    columnList = "effective_from,effective_to")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MerchantFee extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchant;

    @Enumerated(EnumType.STRING)
    @Column(name = "fee_type", nullable = false, length = 15)
    private FeeType feeType;

    @Column(name = "fee_percentage", precision = 6, scale = 4)
    private BigDecimal feePercentage;

    @Column(name = "fee_fixed", precision = 15, scale = 2)
    private BigDecimal feeFixed;

    @Column(name = "min_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal minAmount = BigDecimal.ZERO;

    @Column(name = "max_amount", precision = 15, scale = 2)
    private BigDecimal maxAmount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "XOF";

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;
}
