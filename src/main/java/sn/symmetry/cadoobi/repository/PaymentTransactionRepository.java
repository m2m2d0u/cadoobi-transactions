package sn.symmetry.cadoobi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
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

    List<PaymentTransaction> findByMerchantIdAndStatus(String merchantId, PaymentStatus status);

    List<PaymentTransaction> findByStatus(PaymentStatus status);

    boolean existsByReference(String reference);
}
