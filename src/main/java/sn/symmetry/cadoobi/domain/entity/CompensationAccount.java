package sn.symmetry.cadoobi.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import sn.symmetry.cadoobi.domain.enums.CompensationAccountType;

@Embeddable
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompensationAccount {

    @Enumerated(EnumType.STRING)
    @Column(name = "comp_type", length = 20)
    private CompensationAccountType type;

    // ── Bank account fields ───────────────────────────────────────────────────

    @Column(name = "comp_bank_name", length = 100)
    private String bankName;

    @Column(name = "comp_account_number", length = 50)
    private String accountNumber;

    @Column(name = "comp_account_holder", length = 150)
    private String accountHolder;

    @Column(name = "comp_iban", length = 34)
    private String iban;

    @Column(name = "comp_swift", length = 11)
    private String swift;

    // ── Operator (mobile money) fields ────────────────────────────────────────

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comp_operator_id")
    private Operator operator;

    @Column(name = "comp_operator_phone", length = 15)
    private String operatorPhone;

    @Column(name = "comp_operator_holder_name", length = 150)
    private String operatorHolderName;
}
