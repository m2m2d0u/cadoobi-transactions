package sn.symmetry.cadoobi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.symmetry.cadoobi.domain.entity.WebhookConfiguration;
import sn.symmetry.cadoobi.domain.enums.NotificationEventType;
import sn.symmetry.cadoobi.repository.WebhookConfigurationRepository;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service responsible for delivering webhook notifications to configured endpoints.
 * Handles payload construction, HMAC-SHA256 signature generation, and delivery.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookDeliveryService {

    private final WebhookConfigurationRepository webhookRepository;
    private final NotificationService notificationService;
    private final EncryptionService encryptionService;
    private final ObjectMapper objectMapper;

    /**
     * Triggers webhooks for a specific event type and user.
     * Finds all active webhooks subscribed to the event and queues them for delivery.
     *
     * @param userId The user ID who owns the webhook configurations
     * @param eventType The event type that triggered the webhook
     * @param eventData The data to include in the webhook payload
     */
    @Async
    @Transactional
    public void triggerWebhooks(UUID userId, NotificationEventType eventType, Object eventData) {
        log.info("Triggering webhooks for user: userId={}, eventType={}", userId, eventType);

        // Find all active webhooks for this user
        List<WebhookConfiguration> webhooks = webhookRepository.findByUserIdAndIsActive(userId, true);

        if (webhooks.isEmpty()) {
            log.debug("No active webhooks configured for user: userId={}", userId);
            return;
        }

        // Process each webhook
        for (WebhookConfiguration webhook : webhooks) {
            try {
                // Check if webhook is subscribed to this event type
                if (!isSubscribedToEvent(webhook, eventType)) {
                    log.debug("Webhook not subscribed to event: webhookId={}, eventType={}",
                            webhook.getId(), eventType);
                    continue;
                }

                // Decrypt the webhook secret
                String webhookSecret;
                try {
                    webhookSecret = encryptionService.decrypt(webhook.getSecret());
                } catch (Exception e) {
                    log.error("Failed to decrypt webhook secret: webhookId={}", webhook.getId(), e);
                    continue;
                }

                // Build webhook payload
                Map<String, Object> payload = buildWebhookPayload(eventType, eventData);
                String payloadJson = objectMapper.writeValueAsString(payload);

                // Generate signature
                String signature = generateSignature(payloadJson, webhookSecret);

                // Create payload with signature header
                Map<String, Object> notificationPayload = new HashMap<>();
                notificationPayload.put("payload", payload);
                notificationPayload.put("signature", signature);
                notificationPayload.put("timestamp", payload.get("timestamp"));

                String notificationJson = objectMapper.writeValueAsString(notificationPayload);

                // Queue notification for delivery
                notificationService.sendNotification(eventType, webhook.getUrl(), notificationJson);

                log.info("Webhook queued for delivery: webhookId={}, eventType={}, url={}",
                        webhook.getId(), eventType, webhook.getUrl());

            } catch (Exception e) {
                log.error("Failed to queue webhook: webhookId={}, eventType={}",
                        webhook.getId(), eventType, e);
            }
        }
    }

    /**
     * Checks if a webhook configuration is subscribed to a specific event type.
     * If no events are configured (null or empty), webhook receives all events.
     */
    private boolean isSubscribedToEvent(WebhookConfiguration webhook, NotificationEventType eventType) {
        String subscribedEvents = webhook.getSubscribedEvents();

        // If no events configured, subscribe to all events
        if (subscribedEvents == null || subscribedEvents.trim().isEmpty()) {
            return true;
        }

        // Check if event type is in the comma-separated list
        String eventTypeStr = eventType.name().toLowerCase();
        String[] events = subscribedEvents.toLowerCase().split(",");

        for (String event : events) {
            if (event.trim().equals(eventTypeStr)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Builds the webhook payload structure with event metadata.
     */
    private Map<String, Object> buildWebhookPayload(NotificationEventType eventType, Object eventData) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", UUID.randomUUID().toString());
        payload.put("type", eventType.name().toLowerCase());
        payload.put("timestamp", Instant.now().toEpochMilli());
        payload.put("data", eventData);

        return payload;
    }

    /**
     * Generates HMAC-SHA256 signature for webhook payload verification.
     * The signature is computed as: HMAC-SHA256(payload, secret) and returned as Base64.
     */
    private String generateSignature(String payload, String secret) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(
                    secret.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            );
            hmac.init(secretKey);

            byte[] signatureBytes = hmac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(signatureBytes);

        } catch (Exception e) {
            log.error("Failed to generate webhook signature", e);
            throw new RuntimeException("Failed to generate webhook signature", e);
        }
    }
}
