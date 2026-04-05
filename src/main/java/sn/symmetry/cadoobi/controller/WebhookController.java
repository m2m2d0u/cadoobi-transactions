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
import sn.symmetry.cadoobi.dto.CreateWebhookConfigurationRequest;
import sn.symmetry.cadoobi.dto.UpdateWebhookConfigurationRequest;
import sn.symmetry.cadoobi.dto.WebhookConfigurationResponse;
import sn.symmetry.cadoobi.dto.common.ControllerApiResponse;
import sn.symmetry.cadoobi.security.CustomUserDetails;
import sn.symmetry.cadoobi.service.WebhookService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/webhooks")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Webhooks", description = "Webhook configuration for event notifications")
public class WebhookController {

    private final WebhookService webhookService;

    @GetMapping
    @Operation(
        summary = "Get all webhooks",
        description = "Returns all webhook configurations for the authenticated user. Secrets are shown in masked format for security."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Webhooks retrieved successfully",
            content = @Content(schema = @Schema(implementation = WebhookConfigurationResponse.class))
        )
    })
    public ResponseEntity<ControllerApiResponse<List<WebhookConfigurationResponse>>> getAllWebhooks(
        Authentication authentication
    ) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        UUID userId = userDetails.getUserId();

        List<WebhookConfigurationResponse> webhooks = webhookService.getAllWebhooks(userId);
        return ResponseEntity.ok(ControllerApiResponse.success(webhooks,
            webhooks.size() + " webhook(s) found"));
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get webhook by ID",
        description = "Returns a specific webhook configuration by its UUID. The secret is masked for security."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Webhook retrieved successfully",
            content = @Content(schema = @Schema(implementation = WebhookConfigurationResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Webhook not found",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        )
    })
    public ResponseEntity<ControllerApiResponse<WebhookConfigurationResponse>> getWebhookById(
        @Parameter(description = "Webhook UUID", required = true)
        @PathVariable UUID id,
        Authentication authentication
    ) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        UUID userId = userDetails.getUserId();

        WebhookConfigurationResponse webhook = webhookService.getWebhookById(userId, id);
        return ResponseEntity.ok(ControllerApiResponse.success(webhook, "Webhook retrieved successfully"));
    }

    @PostMapping
    @Operation(
        summary = "Create webhook",
        description = "Creates a new webhook configuration. The webhook secret is returned only once - store it securely."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Webhook created successfully",
            content = @Content(schema = @Schema(implementation = WebhookConfigurationResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid webhook data",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        )
    })
    public ResponseEntity<ControllerApiResponse<WebhookConfigurationResponse>> createWebhook(
        @Valid @RequestBody CreateWebhookConfigurationRequest request,
        Authentication authentication
    ) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        UUID userId = userDetails.getUserId();

        log.info("Creating webhook for user: userId={}, name={}", userId, request.getName());
        WebhookConfigurationResponse webhook = webhookService.createWebhook(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ControllerApiResponse.created(webhook, "Webhook created successfully. Store the secret securely - it won't be shown again."));
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Update webhook",
        description = "Updates an existing webhook configuration. The secret cannot be changed here - use the regenerate endpoint."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Webhook updated successfully",
            content = @Content(schema = @Schema(implementation = WebhookConfigurationResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid webhook data",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Webhook not found",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        )
    })
    public ResponseEntity<ControllerApiResponse<WebhookConfigurationResponse>> updateWebhook(
        @Parameter(description = "Webhook UUID", required = true)
        @PathVariable UUID id,
        @Valid @RequestBody UpdateWebhookConfigurationRequest request,
        Authentication authentication
    ) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        UUID userId = userDetails.getUserId();

        log.info("Updating webhook: userId={}, webhookId={}", userId, id);
        WebhookConfigurationResponse webhook = webhookService.updateWebhook(userId, id, request);
        return ResponseEntity.ok(ControllerApiResponse.success(webhook, "Webhook updated successfully"));
    }

    @PostMapping("/{id}/regenerate-secret")
    @Operation(
        summary = "Regenerate webhook secret",
        description = "Generates a new secret for the webhook. The old secret will no longer work. The new secret is returned only once."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Secret regenerated successfully",
            content = @Content(schema = @Schema(implementation = WebhookConfigurationResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Webhook not found",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        )
    })
    public ResponseEntity<ControllerApiResponse<WebhookConfigurationResponse>> regenerateSecret(
        @Parameter(description = "Webhook UUID", required = true)
        @PathVariable UUID id,
        Authentication authentication
    ) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        UUID userId = userDetails.getUserId();

        log.info("Regenerating webhook secret: userId={}, webhookId={}", userId, id);
        WebhookConfigurationResponse webhook = webhookService.regenerateSecret(userId, id);
        return ResponseEntity.ok(ControllerApiResponse.success(webhook, "Secret regenerated successfully. Store it securely - it won't be shown again."));
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete webhook",
        description = "Deletes a webhook configuration. This action is permanent and cannot be undone."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Webhook deleted successfully"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Webhook not found",
            content = @Content(schema = @Schema(implementation = ControllerApiResponse.class))
        )
    })
    public ResponseEntity<ControllerApiResponse<Void>> deleteWebhook(
        @Parameter(description = "Webhook UUID", required = true)
        @PathVariable UUID id,
        Authentication authentication
    ) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        UUID userId = userDetails.getUserId();

        log.info("Deleting webhook: userId={}, webhookId={}", userId, id);
        webhookService.deleteWebhook(userId, id);
        return ResponseEntity.ok(ControllerApiResponse.success("Webhook deleted successfully"));
    }
}
