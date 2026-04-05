package sn.symmetry.cadoobi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sn.symmetry.cadoobi.domain.entity.ApiKey;
import sn.symmetry.cadoobi.domain.entity.User;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKey, UUID> {

    Optional<ApiKey> findByApiKey(String apiKey);

    List<ApiKey> findByUserId(UUID userId);

    List<ApiKey> findByUserIdAndIsActiveTrue(UUID userId);

    boolean existsByApiKey(String apiKey);

    Optional<ApiKey> findByApiKeyAndUser_Id(String apiKey, UUID userId);

    Collection<ApiKey> findApiKeysByUser(User user);
}
