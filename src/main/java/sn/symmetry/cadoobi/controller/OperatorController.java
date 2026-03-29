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
import sn.symmetry.cadoobi.dto.*;
import sn.symmetry.cadoobi.service.OperatorFeeService;
import sn.symmetry.cadoobi.service.OperatorService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/operators")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Operators", description = "Operator management endpoints for configuring payment operators and their fee structures")
public class OperatorController {

    private final OperatorService operatorService;
    private final OperatorFeeService operatorFeeService;

    @GetMapping
    @Operation(
        summary = "List all active operators",
        description = "Retrieves a list of all active payment operators available in the system"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Active operators retrieved successfully",
            content = @Content(schema = @Schema(implementation = OperatorResponse.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        )
    })
    public ResponseEntity<ControllerApiResponse<List<OperatorResponse>>> getAllActiveOperators() {
        log.info("Fetching all active operators");

        List<OperatorResponse> operators = operatorService.getAllActiveOperators();

        ControllerApiResponse<List<OperatorResponse>> response = ControllerApiResponse.success(
            operators,
            operators.size() + " active operator(s) found"
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get operator by ID",
        description = "Retrieves detailed information about a specific payment operator"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Operator retrieved successfully",
            content = @Content(schema = @Schema(implementation = OperatorResponse.class))
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
    public ResponseEntity<ControllerApiResponse<OperatorResponse>> getOperatorById(
        @Parameter(description = "Unique operator identifier (UUID)", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable UUID id
    ) {
        log.info("Fetching operator by id: {}", id);

        OperatorResponse operator = operatorService.getOperatorById(id);

        ControllerApiResponse<OperatorResponse> response = ControllerApiResponse.success(
            operator,
            "Operator retrieved successfully"
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping
    @Operation(
        summary = "Create new operator",
        description = "Registers a new payment operator in the system with its configuration details"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Operator created successfully",
            content = @Content(schema = @Schema(implementation = OperatorResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid operator configuration",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Operator with this code already exists",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        )
    })
    public ResponseEntity<ControllerApiResponse<OperatorResponse>> createOperator(
        @Valid @RequestBody CreateOperatorRequest request
    ) {
        log.info("Creating new operator: code={}, name={}", request.getCode(), request.getName());

        OperatorResponse operator = operatorService.createOperator(request);

        ControllerApiResponse<OperatorResponse> response = ControllerApiResponse.created(
            operator,
            "Operator created successfully"
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}/fees")
    @Operation(
        summary = "Get operator fee configurations",
        description = "Retrieves all fee configurations for a specific payment operator"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Fee configurations retrieved successfully",
            content = @Content(schema = @Schema(implementation = OperatorFeeResponse.class))
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
    public ResponseEntity<ControllerApiResponse<List<OperatorFeeResponse>>> getOperatorFees(
        @Parameter(description = "Unique operator identifier (UUID)", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable UUID id
    ) {
        log.info("Fetching fees for operator: {}", id);

        List<OperatorFeeResponse> fees = operatorFeeService.getOperatorFees(id);

        ControllerApiResponse<List<OperatorFeeResponse>> response = ControllerApiResponse.success(
            fees,
            fees.size() + " fee configuration(s) found"
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/fees")
    @Operation(
        summary = "Create operator fee configuration",
        description = "Creates a new fee configuration for a specific payment operator"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Fee configuration created successfully",
            content = @Content(schema = @Schema(implementation = OperatorFeeResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid fee configuration",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Operator not found",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Fee configuration already exists for this operation type",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        )
    })
    public ResponseEntity<ControllerApiResponse<OperatorFeeResponse>> createOperatorFee(
        @Parameter(description = "Unique operator identifier (UUID)", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable UUID id,
        @Valid @RequestBody CreateOperatorFeeRequest request
    ) {
        log.info("Creating fee for operator: {}, operationType={}, feeType={}",
            id, request.getOperationType(), request.getFeeType());

        OperatorFeeResponse fee = operatorFeeService.createOperatorFee(id, request);

        ControllerApiResponse<OperatorFeeResponse> response = ControllerApiResponse.created(
            fee,
            "Fee configuration created successfully"
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
