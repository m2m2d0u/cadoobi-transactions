package sn.symmetry.cadoobi.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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
    public ResponseEntity<PaymentResponse> initiatePayment(@Valid @RequestBody InitiatePaymentRequest request) {
        log.info("Received payment initiation request: reference={}, merchant={}, operator={}",
            request.getReference(), request.getMerchantId(), request.getOperatorCode());

        PaymentResponse response = paymentService.initiatePayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{reference}")
    public ResponseEntity<PaymentResponse> getPaymentByReference(@PathVariable String reference) {
        log.info("Fetching payment by reference: {}", reference);

        PaymentResponse response = paymentService.getPaymentByReference(reference);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/callbacks/{operatorCode}")
    public ResponseEntity<Void> handleOperatorCallback(
        @PathVariable String operatorCode,
        @RequestBody String payload
    ) {
        log.info("Received callback from operator: {}", operatorCode);
        log.debug("Callback payload: {}", payload);

        return ResponseEntity.ok().build();
    }
}
