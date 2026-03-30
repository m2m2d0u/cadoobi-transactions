package sn.symmetry.cadoobi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.symmetry.cadoobi.dto.common.ControllerApiResponse;
import sn.symmetry.cadoobi.dto.InitiatePaymentRequest;
import sn.symmetry.cadoobi.dto.PaymentResponse;
import sn.symmetry.cadoobi.service.PaymentService;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payments", description = "Payment management endpoints for initiating and tracking payment transactions")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @Operation(
        summary = "Initiate a payment transaction",
        description = "Creates a new payment transaction with the specified merchant and operator details"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Payment initiated successfully",
            content = @Content(schema = @Schema(implementation = PaymentResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request parameters",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Operator not found",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Payment reference already exists",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        )
    })
    public ResponseEntity<ControllerApiResponse<PaymentResponse>> initiatePayment(
        @Valid @RequestBody InitiatePaymentRequest request
    ) {
        log.info("Received payment initiation request: reference={}, merchant={}, operator={}",
            request.getReference(), request.getMerchantId(), request.getOperatorCode());

        PaymentResponse payment = paymentService.initiatePayment(request);

        ControllerApiResponse<PaymentResponse> response = ControllerApiResponse.created(
            payment,
            "Payment initiated successfully"
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{reference}")
    @Operation(
        summary = "Get payment by reference",
        description = "Retrieves payment details using the unique payment reference identifier"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Payment found and retrieved successfully",
            content = @Content(schema = @Schema(implementation = PaymentResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Payment not found",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        )
    })
    public ResponseEntity<ControllerApiResponse<PaymentResponse>> getPaymentByReference(
        @Parameter(description = "Unique payment reference identifier", required = true, example = "PAY-20240326-123456")
        @PathVariable String reference
    ) {
        log.info("Fetching payment by reference: {}", reference);

        PaymentResponse payment = paymentService.getPaymentByReference(reference);

        ControllerApiResponse<PaymentResponse> response = ControllerApiResponse.success(
            payment,
            "Payment retrieved successfully"
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/callbacks/{operatorCode}")
    @Operation(
        summary = "Handle operator callback",
        description = "Receives and processes payment status callbacks from external payment operators"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Callback received and queued for processing",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid callback payload",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Operator not found",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        )
    })
    public ResponseEntity<ControllerApiResponse<Void>> handleOperatorCallback(
        @Parameter(description = "Operator code identifier", required = true, example = "ORANGE_MONEY")
        @PathVariable String operatorCode,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Raw callback payload from the payment operator",
            required = true
        )
        @RequestBody String payload
    ) {
        log.info("Received callback from operator: {}", operatorCode);
        log.debug("Callback payload: {}", payload);

        // TODO: Process callback and update payment status

        ControllerApiResponse<Void> response = ControllerApiResponse.success(
            "Callback received and queued for processing"
        );

        return ResponseEntity.ok(response);
    }
}
