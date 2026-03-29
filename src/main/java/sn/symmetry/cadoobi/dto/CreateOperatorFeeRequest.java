package sn.symmetry.cadoobi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;
import sn.symmetry.cadoobi.domain.enums.FeeType;
import sn.symmetry.cadoobi.domain.enums.OperationType;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request payload for creating an operator fee configuration")
public class CreateOperatorFeeRequest {

    @Schema(description = "Type of operation this fee applies to", example = "PAYIN", required = true)
    @NotNull(message = "Operation type is required")
    private OperationType operationType;

    @Schema(description = "Type of fee calculation method", example = "PERCENTAGE", required = true)
    @NotNull(message = "Fee type is required")
    private FeeType feeType;

    @Schema(description = "Fee percentage (0.0 to 1.0 representing 0% to 100%)", example = "0.025")
    @DecimalMin(value = "0.0", message = "Fee percentage must be non-negative")
    @DecimalMax(value = "1.0", message = "Fee percentage must not exceed 1 (100%)")
    private BigDecimal feePercentage;

    @Schema(description = "Fixed fee amount", example = "100")
    @DecimalMin(value = "0.0", message = "Fixed fee must be non-negative")
    private BigDecimal feeFixed;

    @Schema(description = "Minimum transaction amount", example = "0", required = true, defaultValue = "0")
    @NotNull(message = "Minimum amount is required")
    @DecimalMin(value = "0.0", message = "Minimum amount must be non-negative")
    private BigDecimal minAmount = BigDecimal.ZERO;

    @Schema(description = "Maximum transaction amount", example = "1000000")
    @DecimalMin(value = "0.0", message = "Maximum amount must be non-negative")
    private BigDecimal maxAmount;

    @Schema(description = "Currency code (ISO 4217)", example = "XOF", defaultValue = "XOF")
    @Size(max = 3, message = "Currency must be 3 characters")
    private String currency = "XOF";

    @Schema(description = "Whether this fee configuration is active", example = "true", defaultValue = "true")
    private Boolean isActive = true;

    @Schema(description = "Date from which this fee is effective", example = "2024-01-01", required = true)
    @NotNull(message = "Effective from date is required")
    private LocalDate effectiveFrom;

    @Schema(description = "Date until which this fee is effective", example = "2024-12-31")
    private LocalDate effectiveTo;
}
