package sn.symmetry.cadoobi.dto;

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
public class CreateOperatorFeeRequest {

    @NotNull(message = "Operation type is required")
    private OperationType operationType;

    @NotNull(message = "Fee type is required")
    private FeeType feeType;

    @DecimalMin(value = "0.0", message = "Fee percentage must be non-negative")
    @DecimalMax(value = "1.0", message = "Fee percentage must not exceed 1 (100%)")
    private BigDecimal feePercentage;

    @DecimalMin(value = "0.0", message = "Fixed fee must be non-negative")
    private BigDecimal feeFixed;

    @NotNull(message = "Minimum amount is required")
    @DecimalMin(value = "0.0", message = "Minimum amount must be non-negative")
    private BigDecimal minAmount = BigDecimal.ZERO;

    @DecimalMin(value = "0.0", message = "Maximum amount must be non-negative")
    private BigDecimal maxAmount;

    @Size(max = 3, message = "Currency must be 3 characters")
    private String currency = "XOF";

    private Boolean isActive = true;

    @NotNull(message = "Effective from date is required")
    private LocalDate effectiveFrom;

    private LocalDate effectiveTo;
}
