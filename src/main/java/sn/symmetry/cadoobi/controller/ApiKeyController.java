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
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import sn.symmetry.cadoobi.dto.ApiKeyResponse;
import sn.symmetry.cadoobi.dto.CreateApiKeyRequest;
import sn.symmetry.cadoobi.dto.UpdateApiKeyRequest;
import sn.symmetry.cadoobi.dto.common.ControllerApiResponse;
import sn.symmetry.cadoobi.security.CustomUserDetails;
import sn.symmetry.cadoobi.service.ApiKeyService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api-keys")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "API Keys", description = "API key management for external authentication")
public class ApiKeyController {

    private final ApiKeyService apiKeyService;

    @GetMapping
    @Operation(
        summary = "Get all API keys",
        description = "Returns all API keys for the authenticated user. API keys are shown in masked format for security."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "API keys retrieved successfully",
            content = @Content(schema = @Schema(implementation = ApiKeyResponse.class))
        )
    })
    public ResponseEntity<ControllerApiResponse<List<ApiKeyResponse>>> getAllApiKeys(
        Authentication authentication
    ) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        UUID userId = userDetails.getUserId();

        List<ApiKeyResponse> apiKeys = apiKeyService.getAllApiKeys(userId);
        return ResponseEntity.ok(ControllerApiResponse.success(apiKeys,
            apiKeys.size() + " API key(s) found"));
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get API key by ID",
        description = "Returns a specific API key by its UUID. The key value is masked for security."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "API key retrieved successfully",
            content = @Content(schema = @Schema(implementation = ApiKeyResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "API key not found",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        )
    })
    public ResponseEntity<ControllerApiResponse<ApiKeyResponse>> getApiKeyById(
        @Parameter(description = "API key UUID", required = true)
        @PathVariable UUID id,
        Authentication authentication
    ) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        UUID userId = userDetails.getUserId();

        ApiKeyResponse apiKey = apiKeyService.getApiKeyById(userId, id);
        return ResponseEntity.ok(ControllerApiResponse.success(apiKey, "API key retrieved successfully"));
    }

    @PostMapping
    @Operation(
        summary = "Create API key",
        description = "Creates a new API key for the authenticated user. The full API key is returned only once - store it securely."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "API key created successfully",
            content = @Content(schema = @Schema(implementation = ApiKeyResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid API key data",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        )
    })
    public ResponseEntity<ControllerApiResponse<ApiKeyResponse>> createApiKey(
        @Valid @RequestBody CreateApiKeyRequest request,
        Authentication authentication
    ) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        UUID userId = userDetails.getUserId();

        log.info("Creating API key for user: userId={}, name={}", userId, request.getName());
        ApiKeyResponse apiKey = apiKeyService.createApiKey(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ControllerApiResponse.created(apiKey, "API key created successfully. Store it securely - it won't be shown again."));
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Update API key",
        description = "Updates an existing API key. The key value itself cannot be changed."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "API key updated successfully",
            content = @Content(schema = @Schema(implementation = ApiKeyResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid API key data",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "API key not found",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        )
    })
    public ResponseEntity<ControllerApiResponse<ApiKeyResponse>> updateApiKey(
        @Parameter(description = "API key UUID", required = true)
        @PathVariable UUID id,
        @Valid @RequestBody UpdateApiKeyRequest request,
        Authentication authentication
    ) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        UUID userId = userDetails.getUserId();

        log.info("Updating API key: userId={}, apiKeyId={}", userId, id);
        ApiKeyResponse apiKey = apiKeyService.updateApiKey(userId, id, request);
        return ResponseEntity.ok(ControllerApiResponse.success(apiKey, "API key updated successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete API key",
        description = "Deletes an API key. This action is permanent and cannot be undone."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "API key deleted successfully"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "API key not found",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        )
    })
    public ResponseEntity<ControllerApiResponse<Void>> deleteApiKey(
        @Parameter(description = "API key UUID", required = true)
        @PathVariable UUID id,
        Authentication authentication
    ) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        UUID userId = userDetails.getUserId();

        log.info("Deleting API key: userId={}, apiKeyId={}", userId, id);
        apiKeyService.deleteApiKey(userId, id);
        return ResponseEntity.ok(ControllerApiResponse.success("API key deleted successfully"));
    }
}
