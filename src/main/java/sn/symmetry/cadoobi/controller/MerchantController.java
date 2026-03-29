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
import sn.symmetry.cadoobi.domain.enums.MerchantStatus;
import sn.symmetry.cadoobi.dto.ControllerApiResponse;
import sn.symmetry.cadoobi.dto.CreateMerchantRequest;
import sn.symmetry.cadoobi.dto.MerchantResponse;
import sn.symmetry.cadoobi.dto.UpdateMerchantRequest;
import sn.symmetry.cadoobi.service.MerchantService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/merchants")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Merchants", description = "Merchant management endpoints for creating and managing merchant accounts")
public class MerchantController {

    private final MerchantService merchantService;

    @GetMapping
    @Operation(
        summary = "List all merchants",
        description = "Retrieves all merchants, optionally filtered by status"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Merchants retrieved successfully",
            content = @Content(schema = @Schema(implementation = MerchantResponse.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        )
    })
    public ResponseEntity<ControllerApiResponse<List<MerchantResponse>>> getAllMerchants(
        @Parameter(description = "Filter by merchant status (optional)", example = "ACTIVE")
        @RequestParam(required = false) MerchantStatus status
    ) {
        List<MerchantResponse> merchants = status != null
            ? merchantService.getMerchantsByStatus(status)
            : merchantService.getAllMerchants();

        return ResponseEntity.ok(ControllerApiResponse.success(
            merchants,
            merchants.size() + " merchant(s) found"
        ));
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get merchant by ID",
        description = "Retrieves detailed information about a specific merchant by UUID"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Merchant retrieved successfully",
            content = @Content(schema = @Schema(implementation = MerchantResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Merchant not found",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        )
    })
    public ResponseEntity<ControllerApiResponse<MerchantResponse>> getMerchantById(
        @Parameter(description = "Unique merchant identifier (UUID)", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable UUID id
    ) {
        return ResponseEntity.ok(ControllerApiResponse.success(
            merchantService.getMerchantById(id),
            "Merchant retrieved successfully"
        ));
    }

    @GetMapping("/code/{code}")
    @Operation(
        summary = "Get merchant by code",
        description = "Retrieves detailed information about a specific merchant by merchant code"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Merchant retrieved successfully",
            content = @Content(schema = @Schema(implementation = MerchantResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Merchant not found",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        )
    })
    public ResponseEntity<ControllerApiResponse<MerchantResponse>> getMerchantByCode(
        @Parameter(description = "Merchant code", required = true, example = "MERCH001")
        @PathVariable String code
    ) {
        return ResponseEntity.ok(ControllerApiResponse.success(
            merchantService.getMerchantByCode(code),
            "Merchant retrieved successfully"
        ));
    }

    @PostMapping
    @Operation(
        summary = "Create new merchant",
        description = "Registers a new merchant in the system with business and owner details"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Merchant created successfully",
            content = @Content(schema = @Schema(implementation = MerchantResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid merchant data",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Merchant with this code already exists",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        )
    })
    public ResponseEntity<ControllerApiResponse<MerchantResponse>> createMerchant(
        @Valid @RequestBody CreateMerchantRequest request
    ) {
        log.info("Creating merchant: code={}, name={}", request.getCode(), request.getName());
        MerchantResponse merchant = merchantService.createMerchant(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ControllerApiResponse.created(merchant, "Merchant created successfully"));
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Update merchant",
        description = "Updates merchant details including business information and compensation account"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Merchant updated successfully",
            content = @Content(schema = @Schema(implementation = MerchantResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid merchant data",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Merchant not found",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        )
    })
    public ResponseEntity<ControllerApiResponse<MerchantResponse>> updateMerchant(
        @Parameter(description = "Unique merchant identifier (UUID)", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable UUID id,
        @Valid @RequestBody UpdateMerchantRequest request
    ) {
        log.info("Updating merchant: id={}", id);
        MerchantResponse merchant = merchantService.updateMerchant(id, request);
        return ResponseEntity.ok(ControllerApiResponse.success(merchant, "Merchant updated successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete merchant",
        description = "Deletes a merchant account from the system"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Merchant deleted successfully",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Merchant not found",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        )
    })
    public ResponseEntity<ControllerApiResponse<Void>> deleteMerchant(
        @Parameter(description = "Unique merchant identifier (UUID)", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable UUID id
    ) {
        log.info("Deleting merchant: id={}", id);
        merchantService.deleteMerchant(id);
        return ResponseEntity.ok(ControllerApiResponse.success("Merchant deleted successfully"));
    }
}
