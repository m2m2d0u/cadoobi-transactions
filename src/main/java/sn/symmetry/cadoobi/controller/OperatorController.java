package sn.symmetry.cadoobi.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.symmetry.cadoobi.dto.*;
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
    public ResponseEntity<ApiResponse<List<OperatorResponse>>> getAllActiveOperators() {
        log.info("Fetching all active operators");

        List<OperatorResponse> operators = operatorService.getAllActiveOperators();

        ApiResponse<List<OperatorResponse>> response = ApiResponse.success(
            operators,
            operators.size() + " active operator(s) found"
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OperatorResponse>> getOperatorById(@PathVariable UUID id) {
        log.info("Fetching operator by id: {}", id);

        OperatorResponse operator = operatorService.getOperatorById(id);

        ApiResponse<OperatorResponse> response = ApiResponse.success(
            operator,
            "Operator retrieved successfully"
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OperatorResponse>> createOperator(
        @Valid @RequestBody CreateOperatorRequest request
    ) {
        log.info("Creating new operator: code={}, name={}", request.getCode(), request.getName());

        OperatorResponse operator = operatorService.createOperator(request);

        ApiResponse<OperatorResponse> response = ApiResponse.created(
            operator,
            "Operator created successfully"
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}/fees")
    public ResponseEntity<ApiResponse<List<OperatorFeeResponse>>> getOperatorFees(
        @PathVariable UUID id
    ) {
        log.info("Fetching fees for operator: {}", id);

        List<OperatorFeeResponse> fees = operatorFeeService.getOperatorFees(id);

        ApiResponse<List<OperatorFeeResponse>> response = ApiResponse.success(
            fees,
            fees.size() + " fee configuration(s) found"
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/fees")
    public ResponseEntity<ApiResponse<OperatorFeeResponse>> createOperatorFee(
        @PathVariable UUID id,
        @Valid @RequestBody CreateOperatorFeeRequest request
    ) {
        log.info("Creating fee for operator: {}, operationType={}, feeType={}",
            id, request.getOperationType(), request.getFeeType());

        OperatorFeeResponse fee = operatorFeeService.createOperatorFee(id, request);

        ApiResponse<OperatorFeeResponse> response = ApiResponse.created(
            fee,
            "Fee configuration created successfully"
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
