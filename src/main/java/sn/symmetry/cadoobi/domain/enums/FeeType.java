package sn.symmetry.cadoobi.domain.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Type of fee calculation method")
public enum FeeType {
    @Schema(description = "Fee calculated as a percentage of transaction amount")
    PERCENTAGE,

    @Schema(description = "Fixed fee amount regardless of transaction amount")
    FIXED,

    @Schema(description = "Combination of percentage and fixed fee")
    MIXED
}
