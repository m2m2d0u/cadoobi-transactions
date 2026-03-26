package sn.symmetry.cadoobi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.symmetry.cadoobi.domain.entity.Operator;
import sn.symmetry.cadoobi.dto.CreateOperatorRequest;
import sn.symmetry.cadoobi.dto.OperatorResponse;
import sn.symmetry.cadoobi.exception.DuplicateResourceException;
import sn.symmetry.cadoobi.exception.ResourceNotFoundException;
import sn.symmetry.cadoobi.repository.OperatorRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OperatorService {

    private final OperatorRepository operatorRepository;

    @Transactional(readOnly = true)
    public List<OperatorResponse> getAllActiveOperators() {
        return operatorRepository.findByIsActiveTrue().stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OperatorResponse getOperatorById(UUID id) {
        Operator operator = operatorRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Operator not found with id: " + id));
        return toResponse(operator);
    }

    @Transactional(readOnly = true)
    public Operator getOperatorByCode(String code) {
        return operatorRepository.findByCode(code)
            .orElseThrow(() -> new ResourceNotFoundException("Operator not found with code: " + code));
    }

    @Transactional
    public OperatorResponse createOperator(CreateOperatorRequest request) {
        if (operatorRepository.existsByCode(request.getCode())) {
            throw new DuplicateResourceException("Operator with code " + request.getCode() + " already exists");
        }

        Operator operator = Operator.builder()
            .code(request.getCode())
            .name(request.getName())
            .country(request.getCountry())
            .supportsPayin(request.getSupportsPayin())
            .supportsPayout(request.getSupportsPayout())
            .apiBaseUrl(request.getApiBaseUrl())
            .isActive(request.getIsActive())
            .build();

        operator = operatorRepository.save(operator);
        log.info("Created new operator: {} ({})", operator.getName(), operator.getCode());

        return toResponse(operator);
    }

    private OperatorResponse toResponse(Operator operator) {
        return OperatorResponse.builder()
            .id(operator.getId())
            .code(operator.getCode())
            .name(operator.getName())
            .country(operator.getCountry())
            .supportsPayin(operator.getSupportsPayin())
            .supportsPayout(operator.getSupportsPayout())
            .apiBaseUrl(operator.getApiBaseUrl())
            .isActive(operator.getIsActive())
            .createdAt(operator.getCreatedAt())
            .build();
    }
}
