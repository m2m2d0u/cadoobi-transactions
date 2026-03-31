package sn.symmetry.cadoobi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;
import sn.symmetry.cadoobi.domain.enums.FeeType;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request payload for creating a default merchant payout fee template")
public class CreateDefaultMerchantFeeRequest {

    @Schema(description = "Human-readable description of this default fee", example = "Default payout fee (2%)")
    @Size(max = 255)
    private String description;

    @Schema(description = "Type of fee calculation method", example = "PERCENTAGE", required = true)
    @NotNull(message = "Fee type is required")
    private FeeType feeType;

    @Schema(description = "Fee percentage (0.0 to 1.0)", example = "0.02")
    @DecimalMin(value = "0.0", message = "Fee percentage must be non-negative")
    @DecimalMax(value = "1.0", message = "Fee percentage must not exceed 1 (100%)")
    private BigDecimal feePercentage;

    @Schema(description = "Fixed fee amount", example = "100")
    @DecimalMin(value = "0.0", message = "Fixed fee must be non-negative")
    private BigDecimal feeFixed;

    @Schema(description = "Minimum transaction amount", example = "0", defaultValue = "0")
    @NotNull(message = "Minimum amount is required")
    @DecimalMin(value = "0.0", message = "Minimum amount must be non-negative")
    private BigDecimal minAmount = BigDecimal.ZERO;

    @Schema(description = "Maximum transaction amount (null = no limit)", example = "1000000")
    @DecimalMin(value = "0.0", message = "Maximum amount must be non-negative")
    private BigDecimal maxAmount;

    @Schema(description = "Currency code (ISO 4217)", example = "XOF", defaultValue = "XOF")
    @Size(max = 3)
    private String currency = "XOF";

    @Schema(description = "Whether this default fee is active. Only active defaults are copied to new merchants.", example = "true", defaultValue = "true")
    private Boolean isActive = true;

    @Schema(description = "Date until which fees copied from this default remain effective (null = no expiry)", example = "2024-12-31")
    private LocalDate effectiveTo;
}
