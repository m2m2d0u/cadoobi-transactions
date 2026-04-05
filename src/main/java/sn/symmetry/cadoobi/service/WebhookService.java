package sn.symmetry.cadoobi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.symmetry.cadoobi.domain.entity.User;
import sn.symmetry.cadoobi.domain.entity.WebhookConfiguration;
import sn.symmetry.cadoobi.dto.CreateWebhookConfigurationRequest;
import sn.symmetry.cadoobi.dto.UpdateWebhookConfigurationRequest;
import sn.symmetry.cadoobi.dto.WebhookConfigurationResponse;
import sn.symmetry.cadoobi.exception.ResourceNotFoundException;
import sn.symmetry.cadoobi.repository.UserRepository;
import sn.symmetry.cadoobi.repository.WebhookConfigurationRepository;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookService {

    private final WebhookConfigurationRepository webhookRepository;
    private final UserRepository userRepository;
    private final EncryptionService encryptionService;
    private static final String WEBHOOK_SECRET_PREFIX = "whsec_";
    private static final int SECRET_LENGTH = 32; // bytes

    @Transactional
    public WebhookConfigurationResponse createWebhook(UUID userId, CreateWebhookConfigurationRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        String generatedSecret = generateSecret();
        String encryptedSecret = encryptionService.encrypt(generatedSecret);

        String subscribedEventsStr = request.getSubscribedEvents() != null && !request.getSubscribedEvents().isEmpty()
            ? String.join(",", request.getSubscribedEvents())
            : null;

        WebhookConfiguration webhook = WebhookConfiguration.builder()
            .user(user)
            .name(request.getName())
            .url(request.getUrl())
            .description(request.getDescription())
            .secret(encryptedSecret)
            .subscribedEvents(subscribedEventsStr)
            .maxRetries(request.getMaxRetries() != null ? request.getMaxRetries() : 3)
            .timeoutSeconds(request.getTimeoutSeconds() != null ? request.getTimeoutSeconds() : 30)
            .isActive(true)
            .build();

        webhook = webhookRepository.save(webhook);
        log.info("Created webhook configuration for user: userId={}, webhookId={}", userId, webhook.getId());

        // Return response with full secret (only shown once)
        return toResponse(webhook, true, generatedSecret);
    }

    @Transactional(readOnly = true)
    public List<WebhookConfigurationResponse> getAllWebhooks(UUID userId) {
        List<WebhookConfiguration> webhooks = webhookRepository.findByUserId(userId);
        return webhooks.stream()
            .map(webhook -> toResponse(webhook, false, null))
            .toList();
    }

    @Transactional(readOnly = true)
    public WebhookConfigurationResponse getWebhookById(UUID userId, UUID id) {
        WebhookConfiguration webhook = findByIdAndUserId(id, userId);
        return toResponse(webhook, false, null);
    }

    @Transactional
    public WebhookConfigurationResponse updateWebhook(UUID userId, UUID id, UpdateWebhookConfigurationRequest request) {
        WebhookConfiguration webhook = findByIdAndUserId(id, userId);

        if (request.getName() != null) {
            webhook.setName(request.getName());
        }
        if (request.getUrl() != null) {
            webhook.setUrl(request.getUrl());
        }
        if (request.getDescription() != null) {
            webhook.setDescription(request.getDescription());
        }
        if (request.getSubscribedEvents() != null) {
            String subscribedEventsStr = request.getSubscribedEvents().isEmpty()
                ? null
                : String.join(",", request.getSubscribedEvents());
            webhook.setSubscribedEvents(subscribedEventsStr);
        }
        if (request.getIsActive() != null) {
            webhook.setIsActive(request.getIsActive());
        }
        if (request.getMaxRetries() != null) {
            webhook.setMaxRetries(request.getMaxRetries());
        }
        if (request.getTimeoutSeconds() != null) {
            webhook.setTimeoutSeconds(request.getTimeoutSeconds());
        }

        webhook = webhookRepository.save(webhook);
        log.info("Updated webhook configuration: userId={}, webhookId={}", userId, id);
        return toResponse(webhook, false, null);
    }

    @Transactional
    public void deleteWebhook(UUID userId, UUID id) {
        WebhookConfiguration webhook = findByIdAndUserId(id, userId);
        webhookRepository.delete(webhook);
        log.info("Deleted webhook configuration: userId={}, webhookId={}", userId, id);
    }

    @Transactional
    public WebhookConfigurationResponse regenerateSecret(UUID userId, UUID id) {
        WebhookConfiguration webhook = findByIdAndUserId(id, userId);
        String newSecret = generateSecret();
        String encryptedSecret = encryptionService.encrypt(newSecret);
        webhook.setSecret(encryptedSecret);
        webhook = webhookRepository.save(webhook);
        log.info("Regenerated webhook secret: userId={}, webhookId={}", userId, id);
        return toResponse(webhook, true, newSecret);
    }

    @Transactional
    public void updateLastTriggered(UUID id) {
        webhookRepository.findById(id).ifPresent(webhook -> {
            webhook.setLastTriggeredAt(Instant.now());
            webhookRepository.save(webhook);
        });
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    private WebhookConfiguration findByIdAndUserId(UUID id, UUID userId) {
        WebhookConfiguration webhook = webhookRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Webhook configuration not found with id: " + id));

        if (!webhook.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Webhook configuration not found with id: " + id);
        }

        return webhook;
    }

    private String generateSecret() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[SECRET_LENGTH];
        random.nextBytes(bytes);
        String encoded = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        return WEBHOOK_SECRET_PREFIX + encoded;
    }

    private String maskSecret(String plaintextSecret) {
        if (plaintextSecret == null || plaintextSecret.length() < 10) {
            return "****";
        }
        String prefix = plaintextSecret.substring(0, Math.min(10, plaintextSecret.length()));
        String suffix = plaintextSecret.substring(Math.max(plaintextSecret.length() - 4, 10));
        return prefix + "****...****" + suffix;
    }

    private WebhookConfigurationResponse toResponse(WebhookConfiguration webhook, boolean includeFullSecret, String plaintextSecret) {
        List<String> subscribedEventsList = webhook.getSubscribedEvents() != null && !webhook.getSubscribedEvents().isEmpty()
            ? List.of(webhook.getSubscribedEvents().split(","))
            : List.of();

        // For masking, decrypt the secret if we don't have the plaintext
        String secretForMasking = plaintextSecret;
        if (secretForMasking == null) {
            try {
                secretForMasking = encryptionService.decrypt(webhook.getSecret());
            } catch (Exception e) {
                log.error("Error decrypting webhook secret for masking", e);
                secretForMasking = "whsec_****";
            }
        }

        return WebhookConfigurationResponse.builder()
            .id(webhook.getId())
            .name(webhook.getName())
            .url(webhook.getUrl())
            .description(webhook.getDescription())
            .secret(includeFullSecret ? plaintextSecret : null)
            .maskedSecret(maskSecret(secretForMasking))
            .subscribedEvents(subscribedEventsList)
            .isActive(webhook.getIsActive())
            .lastTriggeredAt(webhook.getLastTriggeredAt())
            .maxRetries(webhook.getMaxRetries())
            .timeoutSeconds(webhook.getTimeoutSeconds())
            .createdAt(webhook.getCreatedAt())
            .updatedAt(webhook.getUpdatedAt())
            .build();
    }
}
