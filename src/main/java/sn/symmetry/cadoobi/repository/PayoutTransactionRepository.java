package sn.symmetry.cadoobi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sn.symmetry.cadoobi.domain.entity.PayoutTransaction;
import sn.symmetry.cadoobi.domain.enums.PayoutStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PayoutTransactionRepository extends JpaRepository<PayoutTransaction, UUID> {

    Optional<PayoutTransaction> findByRedemptionId(UUID redemptionId);

    Optional<PayoutTransaction> findByIdempotencyKey(String idempotencyKey);

    List<PayoutTransaction> findByMerchantId(String merchantId);

    List<PayoutTransaction> findByStatus(PayoutStatus status);

    List<PayoutTransaction> findByMerchantIdAndStatus(String merchantId, PayoutStatus status);

    boolean existsByIdempotencyKey(String idempotencyKey);
}
