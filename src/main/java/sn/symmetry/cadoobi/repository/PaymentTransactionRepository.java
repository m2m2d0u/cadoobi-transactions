package sn.symmetry.cadoobi.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.symmetry.cadoobi.domain.entity.PaymentTransaction;
import sn.symmetry.cadoobi.domain.enums.PaymentStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, UUID> {

    Optional<PaymentTransaction> findByReference(String reference);

    Optional<PaymentTransaction> findByOperatorTransactionId(String operatorTransactionId);

    List<PaymentTransaction> findByMerchantIdAndStatus(UUID merchantId, PaymentStatus status);

    List<PaymentTransaction> findByStatus(PaymentStatus status);

    boolean existsByReference(String reference);

    /**
     * Get all payment transactions for merchants managed by a specific user
     */
    @Query("SELECT pt FROM PaymentTransaction pt " +
           "WHERE pt.merchant.user.id = :userId " +
           "ORDER BY pt.createdAt DESC")
    Page<PaymentTransaction> findByMerchantUserId(@Param("userId") UUID userId, Pageable pageable);

    /**
     * Get payment transactions by merchant ID with pagination
     */
    Page<PaymentTransaction> findByMerchantId(UUID merchantId, Pageable pageable);

    /**
     * Get payment transactions by status with pagination
     */
    Page<PaymentTransaction> findByStatus(PaymentStatus status, Pageable pageable);
}
