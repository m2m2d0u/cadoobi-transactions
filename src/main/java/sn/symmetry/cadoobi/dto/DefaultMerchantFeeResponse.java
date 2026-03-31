package sn.symmetry.cadoobi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import sn.symmetry.cadoobi.domain.enums.FeeType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Default merchant payout fee template applied to every new merchant at creation")
public class DefaultMerchantFeeResponse {

    @Schema(description = "Unique identifier")
    private UUID id;

    @Schema(description = "Human-readable description", example = "Default payout fee (2%)")
    private String description;

    @Schema(description = "Fee calculation type", example = "PERCENTAGE")
    private FeeType feeType;

    @Schema(description = "Fee percentage (0.0 to 1.0)", example = "0.02")
    private BigDecimal feePercentage;

    @Schema(description = "Fixed fee amount", example = "100")
    private BigDecimal feeFixed;

    @Schema(description = "Minimum transaction amount", example = "0")
    private BigDecimal minAmount;

    @Schema(description = "Maximum transaction amount", example = "1000000")
    private BigDecimal maxAmount;

    @Schema(description = "Currency code", example = "XOF")
    private String currency;

    @Schema(description = "Whether this default fee is active (inactive defaults are not copied to new merchants)", example = "true")
    private Boolean isActive;

    @Schema(description = "Effective to date (null = no expiry)", example = "2024-12-31")
    private LocalDate effectiveTo;
}
