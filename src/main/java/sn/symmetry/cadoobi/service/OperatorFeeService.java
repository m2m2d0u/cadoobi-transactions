package sn.symmetry.cadoobi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.symmetry.cadoobi.domain.entity.Operator;
import sn.symmetry.cadoobi.domain.entity.OperatorFee;
import sn.symmetry.cadoobi.domain.enums.FeeType;
import sn.symmetry.cadoobi.domain.enums.OperationType;
import sn.symmetry.cadoobi.dto.CreateOperatorFeeRequest;
import sn.symmetry.cadoobi.dto.OperatorFeeResponse;
import sn.symmetry.cadoobi.exception.BusinessException;
import sn.symmetry.cadoobi.exception.ResourceNotFoundException;
import sn.symmetry.cadoobi.repository.OperatorFeeRepository;
import sn.symmetry.cadoobi.repository.OperatorRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OperatorFeeService {

    private final OperatorFeeRepository operatorFeeRepository;
    private final OperatorRepository operatorRepository;

    @Transactional(readOnly = true)
    public List<OperatorFeeResponse> getOperatorFees(UUID operatorId) {
        return operatorFeeRepository.findByOperatorId(operatorId).stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    @Transactional
    public OperatorFeeResponse createOperatorFee(UUID operatorId, CreateOperatorFeeRequest request) {
        Operator operator = operatorRepository.findById(operatorId)
            .orElseThrow(() -> new ResourceNotFoundException("Operator not found with id: " + operatorId));

        validateFeeRequest(request);

        OperatorFee fee = OperatorFee.builder()
            .operator(operator)
            .operationType(request.getOperationType())
            .feeType(request.getFeeType())
            .feePercentage(request.getFeePercentage())
            .feeFixed(request.getFeeFixed())
            .minAmount(request.getMinAmount())
            .maxAmount(request.getMaxAmount())
            .currency(request.getCurrency())
            .isActive(request.getIsActive())
            .effectiveFrom(request.getEffectiveFrom())
            .effectiveTo(request.getEffectiveTo())
            .build();

        fee = operatorFeeRepository.save(fee);
        log.info("Created fee for operator {} ({}): {} - {}",
            operator.getName(), operator.getCode(), request.getOperationType(), request.getFeeType());

        return toResponse(fee);
    }

    @Transactional(readOnly = true)
    public BigDecimal calculateFee(UUID operatorId, OperationType operationType, BigDecimal amount) {
        OperatorFee fee = operatorFeeRepository.findApplicableFee(
            operatorId, operationType, amount, LocalDate.now()
        ).orElse(null);

        if (fee == null) {
            log.debug("No fee configuration found for operator {}, operation {}, amount {}",
                operatorId, operationType, amount);
            return BigDecimal.ZERO;
        }

        BigDecimal computedFee = calculateOperatorFee(amount, fee);

        return computedFee.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateOperatorFee(BigDecimal amount, OperatorFee fee) {
        BigDecimal computedFee = BigDecimal.ZERO;

        if (fee.getFeeType() == FeeType.PERCENTAGE || fee.getFeeType() == FeeType.MIXED) {
            if (fee.getFeePercentage() != null) {
                computedFee = computedFee.add(amount.multiply(fee.getFeePercentage()));
            }
        }

        if (fee.getFeeType() == FeeType.FIXED || fee.getFeeType() == FeeType.MIXED) {
            if (fee.getFeeFixed() != null) {
                computedFee = computedFee.add(fee.getFeeFixed());
            }
        }
        return computedFee;
    }

    private void validateFeeRequest(CreateOperatorFeeRequest request) {
        if (request.getFeeType() == FeeType.PERCENTAGE || request.getFeeType() == FeeType.MIXED) {
            if (request.getFeePercentage() == null) {
                throw new BusinessException("Fee percentage is required for PERCENTAGE or MIXED fee type");
            }
        }

        if (request.getFeeType() == FeeType.FIXED || request.getFeeType() == FeeType.MIXED) {
            if (request.getFeeFixed() == null) {
                throw new BusinessException("Fixed fee is required for FIXED or MIXED fee type");
            }
        }

        if (request.getEffectiveTo() != null && request.getEffectiveTo().isBefore(request.getEffectiveFrom())) {
            throw new BusinessException("Effective to date must be after effective from date");
        }

        if (request.getMaxAmount() != null && request.getMaxAmount().compareTo(request.getMinAmount()) < 0) {
            throw new BusinessException("Maximum amount must be greater than or equal to minimum amount");
        }
    }

    private OperatorFeeResponse toResponse(OperatorFee fee) {
        return OperatorFeeResponse.builder()
            .id(fee.getId())
            .operatorId(fee.getOperator().getId())
            .operationType(fee.getOperationType())
            .feeType(fee.getFeeType())
            .feePercentage(fee.getFeePercentage())
            .feeFixed(fee.getFeeFixed())
            .minAmount(fee.getMinAmount())
            .maxAmount(fee.getMaxAmount())
            .currency(fee.getCurrency())
            .isActive(fee.getIsActive())
            .effectiveFrom(fee.getEffectiveFrom())
            .effectiveTo(fee.getEffectiveTo())
            .build();
    }
}
