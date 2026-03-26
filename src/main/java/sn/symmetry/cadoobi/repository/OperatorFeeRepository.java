package sn.symmetry.cadoobi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.symmetry.cadoobi.domain.entity.OperatorFee;
import sn.symmetry.cadoobi.domain.enums.OperationType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OperatorFeeRepository extends JpaRepository<OperatorFee, UUID> {

    List<OperatorFee> findByOperatorId(UUID operatorId);

    List<OperatorFee> findByOperatorIdAndOperationType(UUID operatorId, OperationType operationType);

    @Query("SELECT f FROM OperatorFee f WHERE f.operator.id = :operatorId " +
           "AND f.operationType = :operationType " +
           "AND f.isActive = true " +
           "AND f.effectiveFrom <= :date " +
           "AND (f.effectiveTo IS NULL OR f.effectiveTo >= :date) " +
           "AND :amount >= f.minAmount " +
           "AND (f.maxAmount IS NULL OR :amount <= f.maxAmount)")
    Optional<OperatorFee> findApplicableFee(
        @Param("operatorId") UUID operatorId,
        @Param("operationType") OperationType operationType,
        @Param("amount") BigDecimal amount,
        @Param("date") LocalDate date
    );
}
