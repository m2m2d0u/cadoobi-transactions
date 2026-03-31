package sn.symmetry.cadoobi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.symmetry.cadoobi.dto.LedgerEntryResponse;
import sn.symmetry.cadoobi.dto.MerchantBalanceResponse;
import sn.symmetry.cadoobi.dto.common.ControllerApiResponse;
import sn.symmetry.cadoobi.service.LedgerService;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Ledger", description = "Merchant ledger balance and entry history")
public class LedgerController {

    private final LedgerService ledgerService;

    @GetMapping("/merchants/{merchantId}/ledger/balances")
    @Operation(summary = "Get all merchant balances",
        description = "Returns all ledger account balances for a merchant (one per currency)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Balances retrieved successfully",
            content = @Content(schema = @Schema(implementation = MerchantBalanceResponse.class))),
        @ApiResponse(responseCode = "404", description = "Merchant not found",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class)))
    })
    public ResponseEntity<ControllerApiResponse<List<MerchantBalanceResponse>>> getAllBalances(
        @Parameter(description = "Merchant UUID", required = true) @PathVariable UUID merchantId
    ) {
        List<MerchantBalanceResponse> balances = ledgerService.getAllBalances(merchantId);
        return ResponseEntity.ok(ControllerApiResponse.success(balances,
            balances.size() + " account(s) found"));
    }

    @GetMapping("/merchants/{merchantId}/ledger/balance")
    @Operation(summary = "Get merchant balance for a currency",
        description = "Returns the ledger balance for a specific merchant and currency")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Balance retrieved successfully",
            content = @Content(schema = @Schema(implementation = MerchantBalanceResponse.class))),
        @ApiResponse(responseCode = "404", description = "Merchant or account not found",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class)))
    })
    public ResponseEntity<ControllerApiResponse<MerchantBalanceResponse>> getBalance(
        @Parameter(description = "Merchant UUID", required = true) @PathVariable UUID merchantId,
        @Parameter(description = "Currency code (ISO 4217)", example = "XOF")
        @RequestParam(defaultValue = "XOF") String currency
    ) {
        MerchantBalanceResponse balance = ledgerService.getBalance(merchantId, currency);
        return ResponseEntity.ok(ControllerApiResponse.success(balance, "Balance retrieved successfully"));
    }

    @GetMapping("/merchants/{merchantId}/ledger/entries")
    @Operation(summary = "Get merchant ledger entries",
        description = "Returns paginated ledger history for a merchant")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Entries retrieved successfully",
            content = @Content(schema = @Schema(implementation = LedgerEntryResponse.class))),
        @ApiResponse(responseCode = "404", description = "Merchant or account not found",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class)))
    })
    public ResponseEntity<ControllerApiResponse<Page<LedgerEntryResponse>>> getEntries(
        @Parameter(description = "Merchant UUID", required = true) @PathVariable UUID merchantId,
        @Parameter(description = "Currency code (ISO 4217)", example = "XOF")
        @RequestParam(defaultValue = "XOF") String currency,
        @PageableDefault(size = 20, sort = "createdAt") Pageable pageable
    ) {
        Page<LedgerEntryResponse> entries = ledgerService.getEntries(merchantId, currency, pageable);
        return ResponseEntity.ok(ControllerApiResponse.success(entries,
            entries.getTotalElements() + " entry/entries found"));
    }
}
