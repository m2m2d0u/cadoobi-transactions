package sn.symmetry.cadoobi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sn.symmetry.cadoobi.domain.entity.GiftCard;
import sn.symmetry.cadoobi.domain.enums.CardStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GiftCardRepository extends JpaRepository<GiftCard, UUID> {

    Optional<GiftCard> findByCardCode(String cardCode);

    Optional<GiftCard> findByPaymentTransactionId(UUID paymentTransactionId);

    List<GiftCard> findByMerchantId(String merchantId);

    List<GiftCard> findByMerchantIdAndStatus(String merchantId, CardStatus status);

    List<GiftCard> findByStatus(CardStatus status);

    boolean existsByCardCode(String cardCode);
}
