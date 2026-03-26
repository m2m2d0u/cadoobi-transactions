package sn.symmetry.cadoobi.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "operators", indexes = {
    @Index(name = "idx_operator_code", columnList = "code"),
    @Index(name = "idx_operator_country", columnList = "country"),
    @Index(name = "idx_operator_active", columnList = "is_active")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Operator extends BaseEntity {

    @Column(name = "code", nullable = false, unique = true, length = 30)
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "country", nullable = false, length = 2)
    private String country;

    @Column(name = "supports_payin", nullable = false)
    private Boolean supportsPayin = true;

    @Column(name = "supports_payout", nullable = false)
    private Boolean supportsPayout = true;

    @Column(name = "api_base_url", length = 500)
    private String apiBaseUrl;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
