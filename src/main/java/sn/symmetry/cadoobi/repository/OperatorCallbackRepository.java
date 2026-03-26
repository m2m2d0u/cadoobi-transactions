package sn.symmetry.cadoobi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sn.symmetry.cadoobi.domain.entity.OperatorCallback;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OperatorCallbackRepository extends JpaRepository<OperatorCallback, UUID> {

    Optional<OperatorCallback> findByOperatorReference(String operatorReference);

    List<OperatorCallback> findByPaymentTransactionId(UUID paymentTransactionId);

    List<OperatorCallback> findByOperatorIdAndProcessedAtIsNull(UUID operatorId);

    boolean existsByOperatorReference(String operatorReference);
}
