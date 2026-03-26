package sn.symmetry.cadoobi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import sn.symmetry.cadoobi.domain.entity.OutboundNotification;
import sn.symmetry.cadoobi.domain.enums.NotificationEventType;
import sn.symmetry.cadoobi.domain.enums.NotificationStatus;
import sn.symmetry.cadoobi.repository.OutboundNotificationRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final OutboundNotificationRepository notificationRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    private static final int MAX_RETRY_ATTEMPTS = 5;
    private static final long[] RETRY_DELAYS_SECONDS = {5, 30, 120, 600, 3600};

    @Async
    @Transactional
    public void sendNotification(NotificationEventType eventType, String targetUrl, String payload) {
        String eventId = UUID.randomUUID().toString();

        if (notificationRepository.existsByEventId(eventId)) {
            log.warn("Notification already exists with eventId: {}", eventId);
            return;
        }

        OutboundNotification notification = OutboundNotification.builder()
            .eventId(eventId)
            .eventType(eventType)
            .targetUrl(targetUrl)
            .payload(payload)
            .status(NotificationStatus.PENDING)
            .attempts(0)
            .nextRetryAt(Instant.now())
            .build();

        notification = notificationRepository.save(notification);

        log.info("Created outbound notification: eventId={}, eventType={}, targetUrl={}",
            eventId, eventType, targetUrl);

        attemptSend(notification);
    }

    @Scheduled(fixedDelay = 10000)
    @Transactional
    public void retryPendingNotifications() {
        Instant now = Instant.now();
        List<OutboundNotification> pendingNotifications =
            notificationRepository.findPendingNotificationsDueForRetry(now);

        for (OutboundNotification notification : pendingNotifications) {
            attemptSend(notification);
        }
    }

    private void attemptSend(OutboundNotification notification) {
        try {
            restTemplate.postForEntity(
                notification.getTargetUrl(),
                notification.getPayload(),
                String.class
            );

            notification.setStatus(NotificationStatus.SENT);
            notification.setLastAttemptAt(Instant.now());
            notificationRepository.save(notification);

            log.info("Successfully sent notification: eventId={}, attempts={}",
                notification.getEventId(), notification.getAttempts() + 1);

        } catch (Exception e) {
            notification.setAttempts(notification.getAttempts() + 1);
            notification.setLastAttemptAt(Instant.now());

            if (notification.getAttempts() >= MAX_RETRY_ATTEMPTS) {
                notification.setStatus(NotificationStatus.FAILED);
                notification.setNextRetryAt(null);

                log.error("Notification failed after {} attempts: eventId={}",
                    MAX_RETRY_ATTEMPTS, notification.getEventId(), e);
            } else {
                long delaySeconds = RETRY_DELAYS_SECONDS[notification.getAttempts() - 1];
                notification.setNextRetryAt(Instant.now().plus(delaySeconds, ChronoUnit.SECONDS));

                log.warn("Notification attempt {} failed, will retry in {}s: eventId={}",
                    notification.getAttempts(), delaySeconds, notification.getEventId());
            }

            notificationRepository.save(notification);
        }
    }
}
