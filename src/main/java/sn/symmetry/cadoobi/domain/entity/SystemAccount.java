package sn.symmetry.cadoobi.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "system_accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemAccount extends BaseEntity {

    @Column(name = "currency", nullable = false, unique = true, length = 3)
    private String currency;

    @Column(name = "balance", nullable = false, precision = 15, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;
}
// Now I need to have the system account that save the earnings