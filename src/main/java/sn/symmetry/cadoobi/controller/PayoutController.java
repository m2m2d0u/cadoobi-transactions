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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.symmetry.cadoobi.domain.enums.PayoutStatus;
import sn.symmetry.cadoobi.dto.CreatePayoutRequest;
import sn.symmetry.cadoobi.dto.PayoutResponse;
import sn.symmetry.cadoobi.dto.common.ControllerApiResponse;
import sn.symmetry.cadoobi.service.PayoutService;

import java.util.UUID;

@RestController
@RequestMapping("/payouts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payouts", description = "Payout transaction management")
public class PayoutController {

    private final PayoutService payoutService;

    @PostMapping
    @Operation(summary = "Create a payout", description = "Initiates a payout to a merchant's compensation account. Funds are locked from the merchant ledger immediately.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Payout created successfully",
            content = @Content(schema = @Schema(implementation = PayoutResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request or insufficient balance",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))),
        @ApiResponse(responseCode = "404", description = "Merchant or operator not found",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class)))
    })
    public ResponseEntity<ControllerApiResponse<PayoutResponse>> createPayout(
        @Valid @RequestBody CreatePayoutRequest request
    ) {
        log.info("Creating payout: merchant={}, amount={}", request.getMerchantId(), request.getAmount());
        PayoutResponse payout = payoutService.createPayout(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ControllerApiResponse.created(payout, "Payout created successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get payout by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Payout retrieved successfully",
            content = @Content(schema = @Schema(implementation = PayoutResponse.class))),
        @ApiResponse(responseCode = "404", description = "Payout not found",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class)))
    })
    public ResponseEntity<ControllerApiResponse<PayoutResponse>> getPayoutById(
        @Parameter(description = "Payout UUID", required = true) @PathVariable UUID id
    ) {
        return ResponseEntity.ok(ControllerApiResponse.success(
            payoutService.getPayoutById(id), "Payout retrieved successfully"));
    }

    @GetMapping
    @Operation(summary = "List payouts by merchant", description = "Returns paginated payouts for a merchant (by symmetry merchant ID)")
    @ApiResponse(responseCode = "200", description = "Payouts retrieved successfully")
    public ResponseEntity<ControllerApiResponse<Page<PayoutResponse>>> getPayoutsByMerchant(
        @Parameter(description = "Symmetry merchant ID", required = true) @RequestParam String merchantId,
        @PageableDefault(size = 20, sort = "createdAt") Pageable pageable
    ) {
        Page<PayoutResponse> payouts = payoutService.getPayoutsByMerchant(merchantId, pageable);
        return ResponseEntity.ok(ControllerApiResponse.success(payouts,
            payouts.getTotalElements() + " payout(s) found"));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update payout status", description = "Updates payout status (COMPLETED or FAILED). Triggers ledger settlement or lock release.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Status updated successfully",
            content = @Content(schema = @Schema(implementation = PayoutResponse.class))),
        @ApiResponse(responseCode = "404", description = "Payout not found",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class)))
    })
    public ResponseEntity<ControllerApiResponse<PayoutResponse>> updateStatus(
        @Parameter(description = "Payout UUID", required = true) @PathVariable UUID id,
        @Parameter(description = "New status", required = true) @RequestParam PayoutStatus status,
        @Parameter(description = "Operator transaction reference") @RequestParam(required = false) String operatorTransactionId
    ) {
        log.info("Updating payout {} status to {}", id, status);
        PayoutResponse payout = payoutService.updatePayoutStatus(id, status, operatorTransactionId);
        return ResponseEntity.ok(ControllerApiResponse.success(payout, "Payout status updated successfully"));
    }
}
