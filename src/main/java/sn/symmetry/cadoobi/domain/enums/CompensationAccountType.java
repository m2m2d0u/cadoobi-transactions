package sn.symmetry.cadoobi.domain.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Type of compensation account for receiving funds")
public enum CompensationAccountType {
    @Schema(description = "Traditional bank account")
    BANK,

    @Schema(description = "Mobile money operator account")
    OPERATOR
}
