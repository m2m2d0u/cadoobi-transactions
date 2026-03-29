package sn.symmetry.cadoobi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import sn.symmetry.cadoobi.domain.enums.CompensationAccountType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Compensation account details for receiving merchant funds (bank or mobile money)")
public class CompensationAccountDto {

    @Schema(description = "Account type (BANK or OPERATOR/mobile money)", example = "BANK", required = true)
    @NotNull(message = "Compensation account type is required (BANK or OPERATOR)")
    private CompensationAccountType type;

    // ── Bank account fields ───────────────────────────────────────────────────

    @Schema(description = "Bank name", example = "Banque de l'Habitat du Sénégal")
    @Size(max = 100)
    private String bankName;

    @Schema(description = "Bank account number", example = "00101234567890123")
    @Size(max = 50)
    private String accountNumber;

    @Schema(description = "Account holder name", example = "Boutique Fatou SARL")
    @Size(max = 150)
    private String accountHolder;

    @Schema(description = "International Bank Account Number (IBAN)", example = "SN08SN012345678901234567890123")
    @Size(max = 34)
    private String iban;

    @Schema(description = "SWIFT/BIC code", example = "CBAOSNDA")
    @Size(max = 11)
    private String swift;

    // ── Operator (mobile money) fields ────────────────────────────────────────

    @Schema(description = "Mobile money operator code (for OPERATOR type)", example = "ORANGE_MONEY")
    @Size(max = 30)
    private String operatorCode;

    @Schema(description = "Operator display name (populated on response)", example = "Orange Money Senegal")
    private String operatorName;

    @Schema(description = "Mobile money account phone number", example = "+221771234567")
    @Size(max = 15)
    private String operatorPhone;

    @Schema(description = "Mobile money account holder name", example = "Fatou Sall")
    @Size(max = 150)
    private String operatorHolderName;
}
