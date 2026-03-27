package sn.symmetry.cadoobi.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.symmetry.cadoobi.dto.ApiResponse;
import sn.symmetry.cadoobi.dto.InitiatePaymentRequest;
import sn.symmetry.cadoobi.dto.PaymentResponse;
import sn.symmetry.cadoobi.service.PaymentService;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<ApiResponse<PaymentResponse>> initiatePayment(
        @Valid @RequestBody InitiatePaymentRequest request
    ) {
        log.info("Received payment initiation request: merchant={}, operator={}",
            request.getMerchantCode(), request.getOperatorCode());

        PaymentResponse payment = paymentService.initiatePayment(request);

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.created(payment, "Payment initiated successfully"));
    }

    @GetMapping("/{reference}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentByReference(
        @PathVariable String reference
    ) {
        log.info("Fetching payment by reference: {}", reference);

        PaymentResponse payment = paymentService.getPaymentByReference(reference);

        return ResponseEntity.ok(ApiResponse.success(payment, "Payment retrieved successfully"));
    }

    @PostMapping("/callbacks/{operatorCode}")
    public ResponseEntity<ApiResponse<Void>> handleOperatorCallback(
        @PathVariable String operatorCode,
        @RequestBody String payload
    ) {
        log.info("Received callback from operator: {}", operatorCode);
        log.debug("Callback payload: {}", payload);

        // TODO: Process callback and update payment status

        return ResponseEntity.ok(ApiResponse.success("Callback received and queued for processing"));
    }
}
