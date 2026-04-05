package sn.symmetry.cadoobi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sn.symmetry.cadoobi.domain.entity.WebhookConfiguration;

import java.util.List;
import java.util.UUID;

@Repository
public interface WebhookConfigurationRepository extends JpaRepository<WebhookConfiguration, UUID> {

    List<WebhookConfiguration> findByUserId(UUID userId);

    List<WebhookConfiguration> findByUserIdAndIsActiveTrue(UUID userId);

    List<WebhookConfiguration> findByIsActiveTrue();
}
