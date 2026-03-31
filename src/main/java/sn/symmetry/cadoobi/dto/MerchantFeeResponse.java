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
@Schema(description = "Merchant payout fee configuration")
public class MerchantFeeResponse {

    @Schema(description = "Unique fee configuration identifier")
    private UUID id;

    @Schema(description = "Merchant identifier")
    private UUID merchantId;

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

    @Schema(description = "Whether fee configuration is active", example = "true")
    private Boolean isActive;

    @Schema(description = "Effective from date", example = "2024-01-01")
    private LocalDate effectiveFrom;

    @Schema(description = "Effective to date", example = "2024-12-31")
    private LocalDate effectiveTo;
}
