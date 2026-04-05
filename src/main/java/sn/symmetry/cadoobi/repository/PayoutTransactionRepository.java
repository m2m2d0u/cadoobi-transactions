package sn.symmetry.cadoobi.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.symmetry.cadoobi.domain.entity.PayoutTransaction;
import sn.symmetry.cadoobi.domain.enums.PayoutStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PayoutTransactionRepository extends JpaRepository<PayoutTransaction, UUID> {

    Optional<PayoutTransaction> findByIdempotencyKey(String idempotencyKey);

    Page<PayoutTransaction> findByMerchantId(String merchantId, Pageable pageable);

    List<PayoutTransaction> findByStatus(PayoutStatus status);

    boolean existsByIdempotencyKey(String idempotencyKey);

    /**
     * Find all payouts for merchants managed by a specific user
     */
    @Query("SELECT p FROM PayoutTransaction p " +
           "JOIN Merchant m ON m.symmetryMerchantId = p.merchantId " +
           "WHERE m.user.id = :userId " +
           "ORDER BY p.createdAt DESC")
    Page<PayoutTransaction> findByMerchantUserId(@Param("userId") UUID userId, Pageable pageable);
}
