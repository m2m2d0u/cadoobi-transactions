package sn.symmetry.cadoobi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import sn.symmetry.cadoobi.domain.entity.OutboundNotification;
import sn.symmetry.cadoobi.domain.enums.NotificationStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OutboundNotificationRepository extends JpaRepository<OutboundNotification, UUID> {

    Optional<OutboundNotification> findByEventId(String eventId);

    List<OutboundNotification> findByStatus(NotificationStatus status);

    @Query("SELECT n FROM OutboundNotification n WHERE n.status = 'PENDING' " +
           "AND n.nextRetryAt IS NOT NULL " +
           "AND n.nextRetryAt <= :now " +
           "ORDER BY n.nextRetryAt ASC")
    List<OutboundNotification> findPendingNotificationsDueForRetry(Instant now);

    boolean existsByEventId(String eventId);
}
