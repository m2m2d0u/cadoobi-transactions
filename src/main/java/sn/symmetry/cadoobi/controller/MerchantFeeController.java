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
import sn.symmetry.cadoobi.dto.CreateDefaultMerchantFeeRequest;
import sn.symmetry.cadoobi.dto.CreateMerchantFeeRequest;
import sn.symmetry.cadoobi.dto.DefaultMerchantFeeResponse;
import sn.symmetry.cadoobi.dto.MerchantFeeResponse;
import sn.symmetry.cadoobi.dto.common.ControllerApiResponse;
import sn.symmetry.cadoobi.service.MerchantFeeService;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Merchant Fees", description = "Merchant fee configuration and default fee template management")
public class MerchantFeeController {

    private final MerchantFeeService merchantFeeService;

    // ── Per-merchant fees  (/merchants/{id}/fees) ────────────────────────────

    @GetMapping("/merchants/{merchantId}/fees")
    @Operation(summary = "Get merchant fees", description = "Returns all fee configurations for a specific merchant")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Fees retrieved successfully",
            content = @Content(schema = @Schema(implementation = MerchantFeeResponse.class))),
        @ApiResponse(responseCode = "404", description = "Merchant not found",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class)))
    })
    public ResponseEntity<ControllerApiResponse<List<MerchantFeeResponse>>> getMerchantFees(
        @Parameter(description = "Merchant UUID", required = true) @PathVariable UUID merchantId
    ) {
        List<MerchantFeeResponse> fees = merchantFeeService.getMerchantFees(merchantId);
        return ResponseEntity.ok(ControllerApiResponse.success(fees,
            fees.size() + " fee configuration(s) found"));
    }

    @PostMapping("/merchants/{merchantId}/fees")
    @Operation(summary = "Create merchant fee", description = "Adds a fee configuration to a specific merchant")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Fee created successfully",
            content = @Content(schema = @Schema(implementation = MerchantFeeResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid fee data",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))),
        @ApiResponse(responseCode = "404", description = "Merchant not found",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class)))
    })
    public ResponseEntity<ControllerApiResponse<MerchantFeeResponse>> createMerchantFee(
        @Parameter(description = "Merchant UUID", required = true) @PathVariable UUID merchantId,
        @Valid @RequestBody CreateMerchantFeeRequest request
    ) {
        log.info("Creating fee for merchant {}: {}", merchantId, request.getFeeType());
        MerchantFeeResponse fee = merchantFeeService.createMerchantFee(merchantId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ControllerApiResponse.created(fee, "Merchant fee created successfully"));
    }

    @PutMapping("/merchants/{merchantId}/fees/{feeId}")
    @Operation(summary = "Update merchant fee", description = "Updates an existing fee configuration for a merchant")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Fee updated successfully",
            content = @Content(schema = @Schema(implementation = MerchantFeeResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid fee data",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))),
        @ApiResponse(responseCode = "404", description = "Merchant or fee not found",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class)))
    })
    public ResponseEntity<ControllerApiResponse<MerchantFeeResponse>> updateMerchantFee(
        @Parameter(description = "Merchant UUID", required = true) @PathVariable UUID merchantId,
        @Parameter(description = "Fee UUID", required = true) @PathVariable UUID feeId,
        @Valid @RequestBody CreateMerchantFeeRequest request
    ) {
        log.info("Updating fee {} for merchant {}", feeId, merchantId);
        MerchantFeeResponse fee = merchantFeeService.updateMerchantFee(merchantId, feeId, request);
        return ResponseEntity.ok(ControllerApiResponse.success(fee, "Merchant fee updated successfully"));
    }

    @DeleteMapping("/merchants/{merchantId}/fees/{feeId}")
    @Operation(summary = "Delete merchant fee", description = "Removes a fee configuration from a merchant")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Fee deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Merchant or fee not found",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class)))
    })
    public ResponseEntity<ControllerApiResponse<Void>> deleteMerchantFee(
        @Parameter(description = "Merchant UUID", required = true) @PathVariable UUID merchantId,
        @Parameter(description = "Fee UUID", required = true) @PathVariable UUID feeId
    ) {
        log.info("Deleting fee {} for merchant {}", feeId, merchantId);
        merchantFeeService.deleteMerchantFee(merchantId, feeId);
        return ResponseEntity.ok(ControllerApiResponse.success("Merchant fee deleted successfully"));
    }

    // ── Default fee templates  (/merchants/fees/defaults) ────────────────────

    @GetMapping("/merchants/fees/defaults")
    @Operation(summary = "Get default merchant fees",
        description = "Returns all default fee templates. Active defaults are automatically applied to every new merchant.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Default fees retrieved successfully",
            content = @Content(schema = @Schema(implementation = DefaultMerchantFeeResponse.class)))
    })
    public ResponseEntity<ControllerApiResponse<List<DefaultMerchantFeeResponse>>> getAllDefaultFees() {
        List<DefaultMerchantFeeResponse> fees = merchantFeeService.getAllDefaultFees();
        return ResponseEntity.ok(ControllerApiResponse.success(fees,
            fees.size() + " default fee(s) found"));
    }

    @PostMapping("/merchants/fees/defaults")
    @Operation(summary = "Create default merchant fee",
        description = "Creates a default fee template. All active defaults are copied to every new merchant at creation time.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Default fee created successfully",
            content = @Content(schema = @Schema(implementation = DefaultMerchantFeeResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid fee data",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class)))
    })
    public ResponseEntity<ControllerApiResponse<DefaultMerchantFeeResponse>> createDefaultFee(
        @Valid @RequestBody CreateDefaultMerchantFeeRequest request
    ) {
        log.info("Creating default merchant fee: {}", request.getFeeType());
        DefaultMerchantFeeResponse fee = merchantFeeService.createDefaultFee(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ControllerApiResponse.created(fee, "Default merchant fee created successfully"));
    }

    @PutMapping("/merchants/fees/defaults/{id}")
    @Operation(summary = "Update default merchant fee", description = "Updates an existing default fee template")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Default fee updated successfully",
            content = @Content(schema = @Schema(implementation = DefaultMerchantFeeResponse.class))),
        @ApiResponse(responseCode = "404", description = "Default fee not found",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class)))
    })
    public ResponseEntity<ControllerApiResponse<DefaultMerchantFeeResponse>> updateDefaultFee(
        @Parameter(description = "Default fee UUID", required = true) @PathVariable UUID id,
        @Valid @RequestBody CreateDefaultMerchantFeeRequest request
    ) {
        log.info("Updating default merchant fee: {}", id);
        DefaultMerchantFeeResponse fee = merchantFeeService.updateDefaultFee(id, request);
        return ResponseEntity.ok(ControllerApiResponse.success(fee, "Default merchant fee updated successfully"));
    }

    @DeleteMapping("/merchants/fees/defaults/{id}")
    @Operation(summary = "Delete default merchant fee",
        description = "Removes a default fee template. Already-created merchants are not affected.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Default fee deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Default fee not found",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class)))
    })
    public ResponseEntity<ControllerApiResponse<Void>> deleteDefaultFee(
        @Parameter(description = "Default fee UUID", required = true) @PathVariable UUID id
    ) {
        log.info("Deleting default merchant fee: {}", id);
        merchantFeeService.deleteDefaultFee(id);
        return ResponseEntity.ok(ControllerApiResponse.success("Default merchant fee deleted successfully"));
    }
}
