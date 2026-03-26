package sn.symmetry.cadoobi.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.symmetry.cadoobi.dto.CreateOperatorRequest;
import sn.symmetry.cadoobi.dto.CreateOperatorFeeRequest;
import sn.symmetry.cadoobi.dto.OperatorFeeResponse;
import sn.symmetry.cadoobi.dto.OperatorResponse;
import sn.symmetry.cadoobi.service.OperatorFeeService;
import sn.symmetry.cadoobi.service.OperatorService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/operators")
@RequiredArgsConstructor
@Slf4j
public class OperatorController {

    private final OperatorService operatorService;
    private final OperatorFeeService operatorFeeService;

    @GetMapping
    public ResponseEntity<List<OperatorResponse>> getAllActiveOperators() {
        log.info("Fetching all active operators");

        List<OperatorResponse> response = operatorService.getAllActiveOperators();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OperatorResponse> getOperatorById(@PathVariable UUID id) {
        log.info("Fetching operator by id: {}", id);

        OperatorResponse response = operatorService.getOperatorById(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<OperatorResponse> createOperator(@Valid @RequestBody CreateOperatorRequest request) {
        log.info("Creating new operator: code={}, name={}", request.getCode(), request.getName());

        OperatorResponse response = operatorService.createOperator(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}/fees")
    public ResponseEntity<List<OperatorFeeResponse>> getOperatorFees(@PathVariable UUID id) {
        log.info("Fetching fees for operator: {}", id);

        List<OperatorFeeResponse> response = operatorFeeService.getOperatorFees(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/fees")
    public ResponseEntity<OperatorFeeResponse> createOperatorFee(
        @PathVariable UUID id,
        @Valid @RequestBody CreateOperatorFeeRequest request
    ) {
        log.info("Creating fee for operator: {}, operationType={}, feeType={}",
            id, request.getOperationType(), request.getFeeType());

        OperatorFeeResponse response = operatorFeeService.createOperatorFee(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
