package sn.symmetry.cadoobi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;
import sn.symmetry.cadoobi.domain.entity.Merchant;
import sn.symmetry.cadoobi.dto.common.ControllerApiResponse;
import sn.symmetry.cadoobi.dto.InitiatePaymentRequest;
import sn.symmetry.cadoobi.dto.OperatorCallbackRequest;
import sn.symmetry.cadoobi.dto.PaymentResponse;
import sn.symmetry.cadoobi.security.CustomUserDetails;
import sn.symmetry.cadoobi.service.ApiKeyService;
import sn.symmetry.cadoobi.service.MerchantService;
import sn.symmetry.cadoobi.service.PaymentService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payments", description = "Payment management endpoints for initiating and tracking payment transactions")
public class PaymentController {

    private final PaymentService paymentService;
    private final ObjectMapper objectMapper;
    private final ApiKeyService apiKeyService;
    private final MerchantService merchantService;

    @Operation(
        summary = "Initiate a payment transaction",
        description = "Creates a new payment transaction with the specified merchant and operator details. " +
                     "**Authentication**: This endpoint requires API key authentication via the X-API-Key header. " +
                     "JWT authentication is not supported for payment initiation. " +
                     "To create an API key, use POST /api-keys endpoint."
    )
    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "apiKey")
    @PostMapping
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
        @Parameter(description = "API Key for authentication", required = true)
        @RequestHeader(value = "X-API-Key") String apiKey,
        @Parameter(description = "Referrer URL (optional, for security validation)")
        @RequestHeader(value = "Referer", required = false) String referrer,
        @Valid @RequestBody InitiatePaymentRequest request
    ) {
        log.info("Received payment initiation request: merchant={}, operator={}",
            request.getMerchantId(), request.getOperatorCode());

        Merchant merchant = merchantService.findByCode(request.getMerchantCode());
        // Validate API key
        UUID userId = apiKeyService.validateApiKey(apiKey.trim(), referrer, merchant.getUser());
        if (userId == null) {
            log.warn("Invalid or expired API key for payment initiation");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ControllerApiResponse.error("Invalid or expired API key", 401, "UNAUTHORIZED"));
        }

        // Update API key last used timestamp
        apiKeyService.updateLastUsed(apiKey.trim(), userId);

        PaymentResponse payment = paymentService.initiatePayment(request, userId);

        ControllerApiResponse<PaymentResponse> response = ControllerApiResponse.created(
            payment,
            "Payment initiated successfully"
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(
        summary = "Get all payment transactions with role-based access",
        description = "Returns all payment transactions with pagination. SUPER_ADMIN and ADMIN can view all transactions. " +
                     "Other users can only view transactions for their merchant accounts."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Payment transactions retrieved successfully",
            content = @Content(schema = @Schema(implementation = PaymentResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - user not authenticated",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        )
    })
    public ResponseEntity<ControllerApiResponse<List<PaymentResponse>>> getAllTransactions(
        Authentication authentication,
        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        UUID currentUserId = userDetails.getUserId();

        // Check if user has SUPER_ADMIN or ADMIN role
        boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN"))
                       || userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));

        log.info("Fetching payment transactions for user: {}, isAdmin: {}", currentUserId, isAdmin);

        Page<PaymentResponse> transactions = paymentService.getAllTransactions(currentUserId, isAdmin, pageable);

        return ResponseEntity.ok(ControllerApiResponse.paged(
            transactions,
            transactions.getTotalElements() + " payment transaction(s) found"
        ));
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
        description = "Receives and processes payment status callbacks from external payment operators. " +
                     "The endpoint accepts either JSON or raw string payloads and processes them accordingly."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Callback received and processed successfully",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid callback payload",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Operator or payment not found",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Duplicate callback - already processed",
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
            description = "Callback payload from the payment operator (JSON or raw string)",
            required = true
        )
        @RequestBody String payload
    ) {
        log.info("Received callback from operator: {}", operatorCode);
        log.debug("Callback payload: {}", payload);

        try {
            OperatorCallbackRequest request;

            // Try to parse as JSON first
            try {
                request = objectMapper.readValue(payload, OperatorCallbackRequest.class);
                // Store the original raw payload
                if (request.getRawPayload() == null) {
                    request.setRawPayload(payload);
                }
            } catch (Exception e) {
                // If JSON parsing fails, treat as raw string and extract what we can
                log.warn("Failed to parse callback as JSON, treating as raw payload: {}", e.getMessage());

                // Create a minimal request object - customize this based on your operators
                request = OperatorCallbackRequest.builder()
                    .rawPayload(payload)
                    .operatorReference("RAW-" + System.currentTimeMillis())
                    .status("PENDING")
                    .build();
            }

            // Process the callback
            paymentService.handleOperatorCallback(operatorCode, request);

            ControllerApiResponse<Void> response = ControllerApiResponse.success(
                "Callback processed successfully"
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error processing operator callback: operator={}, error={}",
                operatorCode, e.getMessage(), e);
            throw e;
        }
    }
}
