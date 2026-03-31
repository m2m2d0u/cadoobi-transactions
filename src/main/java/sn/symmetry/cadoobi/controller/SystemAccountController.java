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
import sn.symmetry.cadoobi.dto.SystemAccountBalanceResponse;
import sn.symmetry.cadoobi.dto.SystemAccountEntryResponse;
import sn.symmetry.cadoobi.dto.common.ControllerApiResponse;
import sn.symmetry.cadoobi.service.SystemAccountService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "System Account", description = "Platform earnings ledger (system account)")
public class SystemAccountController {

    private final SystemAccountService systemAccountService;

    @GetMapping("/system-account/balances")
    @Operation(summary = "Get all system account balances",
        description = "Returns all system account balances (one per currency)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Balances retrieved successfully",
            content = @Content(schema = @Schema(implementation = SystemAccountBalanceResponse.class)))
    })
    public ResponseEntity<ControllerApiResponse<List<SystemAccountBalanceResponse>>> getAllBalances() {
        List<SystemAccountBalanceResponse> balances = systemAccountService.getAllBalances();
        return ResponseEntity.ok(ControllerApiResponse.success(balances,
            balances.size() + " system account(s) found"));
    }

    @GetMapping("/system-account/balance")
    @Operation(summary = "Get system account balance for a currency",
        description = "Returns the system account balance for a specific currency")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Balance retrieved successfully",
            content = @Content(schema = @Schema(implementation = SystemAccountBalanceResponse.class))),
        @ApiResponse(responseCode = "404", description = "System account not found",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class)))
    })
    public ResponseEntity<ControllerApiResponse<SystemAccountBalanceResponse>> getBalance(
        @Parameter(description = "Currency code (ISO 4217)", example = "XOF")
        @RequestParam(defaultValue = "XOF") String currency
    ) {
        SystemAccountBalanceResponse balance = systemAccountService.getBalance(currency);
        return ResponseEntity.ok(ControllerApiResponse.success(balance, "Balance retrieved successfully"));
    }

    @GetMapping("/system-account/entries")
    @Operation(summary = "Get system account entries",
        description = "Returns paginated system account entry history (platform earnings)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Entries retrieved successfully",
            content = @Content(schema = @Schema(implementation = SystemAccountEntryResponse.class))),
        @ApiResponse(responseCode = "404", description = "System account not found",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class)))
    })
    public ResponseEntity<ControllerApiResponse<Page<SystemAccountEntryResponse>>> getEntries(
        @Parameter(description = "Currency code (ISO 4217)", example = "XOF")
        @RequestParam(defaultValue = "XOF") String currency,
        @PageableDefault(size = 20, sort = "createdAt") Pageable pageable
    ) {
        Page<SystemAccountEntryResponse> entries = systemAccountService.getEntries(currency, pageable);
        return ResponseEntity.ok(ControllerApiResponse.success(entries,
            entries.getTotalElements() + " entry/entries found"));
    }
}
