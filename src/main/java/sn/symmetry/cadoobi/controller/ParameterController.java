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
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.symmetry.cadoobi.dto.*;
import sn.symmetry.cadoobi.dto.common.ControllerApiResponse;
import sn.symmetry.cadoobi.service.MerchantFeeService;
import sn.symmetry.cadoobi.service.ParameterService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/parameters")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Parameters", description = "System parameter configuration and default merchant fee management")
public class ParameterController {

    private final ParameterService parameterService;
    private final MerchantFeeService merchantFeeService;

    // ── System Parameters Management ──────────────────────────────────────────

    @GetMapping
    @Operation(
        summary = "Get all system parameters",
        description = "Returns all system configuration parameters with pagination support"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Parameters retrieved successfully",
            content = @Content(schema = @Schema(implementation = ParameterResponse.class))
        )
    })
    public ResponseEntity<ControllerApiResponse<List<ParameterResponse>>> getAllParameters(
        @PageableDefault(size = 20, sort = "category", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<ParameterResponse> page = parameterService.getAllParameters(pageable);
        return ResponseEntity.ok(ControllerApiResponse.paged(page,
            page.getTotalElements() + " parameter(s) found"));
    }

    @GetMapping("/category/{category}")
    @Operation(
        summary = "Get parameters by category",
        description = "Returns all parameters in a specific category (PAYMENT, PAYOUT, NOTIFICATION, SYSTEM, FEES)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Parameters retrieved successfully",
            content = @Content(schema = @Schema(implementation = ParameterResponse.class))
        )
    })
    public ResponseEntity<ControllerApiResponse<List<ParameterResponse>>> getParametersByCategory(
        @Parameter(description = "Category name", required = true, example = "PAYMENT")
        @PathVariable String category
    ) {
        List<ParameterResponse> parameters = parameterService.getParametersByCategory(category);
        return ResponseEntity.ok(ControllerApiResponse.success(parameters,
            parameters.size() + " parameter(s) found in category " + category));
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get parameter by ID",
        description = "Returns a specific parameter by its UUID"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Parameter retrieved successfully",
            content = @Content(schema = @Schema(implementation = ParameterResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Parameter not found",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        )
    })
    public ResponseEntity<ControllerApiResponse<ParameterResponse>> getParameterById(
        @Parameter(description = "Parameter UUID", required = true)
        @PathVariable UUID id
    ) {
        ParameterResponse parameter = parameterService.getParameterById(id);
        return ResponseEntity.ok(ControllerApiResponse.success(parameter, "Parameter retrieved successfully"));
    }

    @GetMapping("/key/{key}")
    @Operation(
        summary = "Get parameter by key",
        description = "Returns a specific parameter by its key"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Parameter retrieved successfully",
            content = @Content(schema = @Schema(implementation = ParameterResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Parameter not found",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        )
    })
    public ResponseEntity<ControllerApiResponse<ParameterResponse>> getParameterByKey(
        @Parameter(description = "Parameter key", required = true, example = "payment.timeout.seconds")
        @PathVariable String key
    ) {
        ParameterResponse parameter = parameterService.getParameterByKey(key);
        return ResponseEntity.ok(ControllerApiResponse.success(parameter, "Parameter retrieved successfully"));
    }

    @PostMapping
    @Operation(
        summary = "Create parameter",
        description = "Creates a new system parameter"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Parameter created successfully",
            content = @Content(schema = @Schema(implementation = ParameterResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid parameter data",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Parameter key already exists",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        )
    })
    public ResponseEntity<ControllerApiResponse<ParameterResponse>> createParameter(
        @Valid @RequestBody CreateParameterRequest request
    ) {
        log.info("Creating parameter: key={}", request.getKey());
        ParameterResponse parameter = parameterService.createParameter(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ControllerApiResponse.created(parameter, "Parameter created successfully"));
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Update parameter",
        description = "Updates an existing system parameter"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Parameter updated successfully",
            content = @Content(schema = @Schema(implementation = ParameterResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid parameter data",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Parameter not found",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        )
    })
    public ResponseEntity<ControllerApiResponse<ParameterResponse>> updateParameter(
        @Parameter(description = "Parameter UUID", required = true)
        @PathVariable UUID id,
        @Valid @RequestBody UpdateParameterRequest request
    ) {
        log.info("Updating parameter: id={}", id);
        ParameterResponse parameter = parameterService.updateParameter(id, request);
        return ResponseEntity.ok(ControllerApiResponse.success(parameter, "Parameter updated successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete parameter",
        description = "Deletes a parameter. System parameters cannot be deleted."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Parameter deleted successfully"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Cannot delete system parameter",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Parameter not found",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        )
    })
    public ResponseEntity<ControllerApiResponse<Void>> deleteParameter(
        @Parameter(description = "Parameter UUID", required = true)
        @PathVariable UUID id
    ) {
        log.info("Deleting parameter: id={}", id);
        parameterService.deleteParameter(id);
        return ResponseEntity.ok(ControllerApiResponse.success("Parameter deleted successfully"));
    }

    // ── Default Merchant Fee Management ───────────────────────────────────────

    @GetMapping("/merchant-fees/defaults")
    @Operation(
        summary = "Get default merchant fees",
        description = "Returns all default merchant fee templates that are automatically applied to new merchants"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Default fees retrieved successfully",
            content = @Content(schema = @Schema(implementation = DefaultMerchantFeeResponse.class))
        )
    })
    public ResponseEntity<ControllerApiResponse<List<DefaultMerchantFeeResponse>>> getDefaultMerchantFees() {
        List<DefaultMerchantFeeResponse> fees = merchantFeeService.getAllDefaultFees();
        return ResponseEntity.ok(ControllerApiResponse.success(fees,
            fees.size() + " default merchant fee(s) found"));
    }

    @PostMapping("/merchant-fees/defaults")
    @Operation(
        summary = "Create default merchant fee",
        description = "Creates a new default merchant fee template. Active defaults are automatically applied to all new merchants."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Default fee created successfully",
            content = @Content(schema = @Schema(implementation = DefaultMerchantFeeResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid fee data",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        )
    })
    public ResponseEntity<ControllerApiResponse<DefaultMerchantFeeResponse>> createDefaultMerchantFee(
        @Valid @RequestBody CreateDefaultMerchantFeeRequest request
    ) {
        log.info("Creating default merchant fee: {}", request.getFeeType());
        DefaultMerchantFeeResponse fee = merchantFeeService.createDefaultFee(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ControllerApiResponse.created(fee, "Default merchant fee created successfully"));
    }

    @PutMapping("/merchant-fees/defaults/{id}")
    @Operation(
        summary = "Update default merchant fee",
        description = "Updates an existing default merchant fee template"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Default fee updated successfully",
            content = @Content(schema = @Schema(implementation = DefaultMerchantFeeResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid fee data",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Default fee not found",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        )
    })
    public ResponseEntity<ControllerApiResponse<DefaultMerchantFeeResponse>> updateDefaultMerchantFee(
        @Parameter(description = "Default fee UUID", required = true)
        @PathVariable UUID id,
        @Valid @RequestBody CreateDefaultMerchantFeeRequest request
    ) {
        log.info("Updating default merchant fee: {}", id);
        DefaultMerchantFeeResponse fee = merchantFeeService.updateDefaultFee(id, request);
        return ResponseEntity.ok(ControllerApiResponse.success(fee, "Default merchant fee updated successfully"));
    }

    @DeleteMapping("/merchant-fees/defaults/{id}")
    @Operation(
        summary = "Delete default merchant fee",
        description = "Deletes a default merchant fee template. Existing merchants are not affected."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Default fee deleted successfully"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Default fee not found",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        )
    })
    public ResponseEntity<ControllerApiResponse<Void>> deleteDefaultMerchantFee(
        @Parameter(description = "Default fee UUID", required = true)
        @PathVariable UUID id
    ) {
        log.info("Deleting default merchant fee: {}", id);
        merchantFeeService.deleteDefaultFee(id);
        return ResponseEntity.ok(ControllerApiResponse.success("Default merchant fee deleted successfully"));
    }
}
