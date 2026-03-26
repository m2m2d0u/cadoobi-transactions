package sn.symmetry.cadoobi.dto;

import lombok.*;
import sn.symmetry.cadoobi.domain.enums.FeeType;
import sn.symmetry.cadoobi.domain.enums.OperationType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OperatorFeeResponse {

    private UUID id;
    private UUID operatorId;
    private OperationType operationType;
    private FeeType feeType;
    private BigDecimal feePercentage;
    private BigDecimal feeFixed;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private String currency;
    private Boolean isActive;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
}
