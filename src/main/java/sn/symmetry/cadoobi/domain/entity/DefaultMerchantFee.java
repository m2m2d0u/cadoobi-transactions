package sn.symmetry.cadoobi.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import sn.symmetry.cadoobi.domain.enums.FeeType;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Default fee template applied to every new merchant at creation time.
 * Merchant fees are payout fees only — no operation type needed.
 * All active records are copied into merchant_fees for the new merchant.
 */
@Entity
@Table(name = "default_merchant_fees",
    indexes = {
        @Index(name = "idx_default_fee_active", columnList = "is_active")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DefaultMerchantFee extends BaseEntity {

    @Column(name = "description", length = 255)
    private String description;

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

    @Column(name = "effective_to")
    private LocalDate effectiveTo;
}
