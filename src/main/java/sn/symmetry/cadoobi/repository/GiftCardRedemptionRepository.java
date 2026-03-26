package sn.symmetry.cadoobi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sn.symmetry.cadoobi.domain.entity.GiftCardRedemption;
import sn.symmetry.cadoobi.domain.enums.RedemptionStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GiftCardRedemptionRepository extends JpaRepository<GiftCardRedemption, UUID> {

    Optional<GiftCardRedemption> findByIdempotencyKey(String idempotencyKey);

    List<GiftCardRedemption> findByGiftCardId(UUID giftCardId);

    List<GiftCardRedemption> findByGiftCardIdAndStatus(UUID giftCardId, RedemptionStatus status);

    List<GiftCardRedemption> findByMerchantId(String merchantId);

    boolean existsByIdempotencyKey(String idempotencyKey);
}
