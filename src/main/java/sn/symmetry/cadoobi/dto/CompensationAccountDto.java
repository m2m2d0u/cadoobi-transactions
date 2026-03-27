package sn.symmetry.cadoobi.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import sn.symmetry.cadoobi.domain.enums.CompensationAccountType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompensationAccountDto {

    @NotNull(message = "Compensation account type is required (BANK or OPERATOR)")
    private CompensationAccountType type;

    // ── Bank account fields ───────────────────────────────────────────────────

    @Size(max = 100)
    private String bankName;

    @Size(max = 50)
    private String accountNumber;

    @Size(max = 150)
    private String accountHolder;

    @Size(max = 34)
    private String iban;

    @Size(max = 11)
    private String swift;

    // ── Operator (mobile money) fields ────────────────────────────────────────

    /** Operator code used on input to resolve the Operator entity (e.g. WAVE, OM, FREE). */
    @Size(max = 30)
    private String operatorCode;

    /** Populated on response — the operator's display name. */
    private String operatorName;

    @Size(max = 15)
    private String operatorPhone;

    @Size(max = 150)
    private String operatorHolderName;
}
