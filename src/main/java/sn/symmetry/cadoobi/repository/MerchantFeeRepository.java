package sn.symmetry.cadoobi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.symmetry.cadoobi.domain.entity.MerchantFee;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MerchantFeeRepository extends JpaRepository<MerchantFee, UUID> {

    List<MerchantFee> findByMerchantId(UUID merchantId);

    @Query("SELECT f FROM MerchantFee f WHERE f.merchant.id = :merchantId " +
           "AND f.isActive = true " +
           "AND f.effectiveFrom <= :date " +
           "AND (f.effectiveTo IS NULL OR f.effectiveTo >= :date) " +
           "AND :amount >= f.minAmount " +
           "AND (f.maxAmount IS NULL OR :amount <= f.maxAmount)")
    Optional<MerchantFee> findApplicableFee(
        @Param("merchantId") UUID merchantId,
        @Param("amount")     BigDecimal amount,
        @Param("date")       LocalDate date
    );
}
