package sn.symmetry.cadoobi.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.symmetry.cadoobi.domain.entity.LedgerEntry;
import sn.symmetry.cadoobi.domain.enums.LedgerDirection;
import sn.symmetry.cadoobi.domain.enums.LedgerEntryType;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, UUID> {

    boolean existsByIdempotencyKey(String idempotencyKey);

    Optional<LedgerEntry> findByIdempotencyKey(String idempotencyKey);

    Page<LedgerEntry> findByMerchantAccountId(UUID merchantAccountId, Pageable pageable);

    Page<LedgerEntry> findByMerchantAccountIdAndDirection(UUID merchantAccountId, LedgerDirection direction, Pageable pageable);

    Page<LedgerEntry> findByMerchantAccountIdAndEntryType(UUID merchantAccountId, LedgerEntryType entryType, Pageable pageable);

    Page<LedgerEntry> findByMerchantAccountIdAndCreatedAtBetween(UUID merchantAccountId, Instant from, Instant to, Pageable pageable);

    /**
     * Get all ledger entries for merchants managed by a specific user
     */
    @Query("SELECT le FROM LedgerEntry le " +
           "WHERE le.merchantAccount.merchant.user.id = :userId " +
           "ORDER BY le.createdAt DESC")
    Page<LedgerEntry> findByMerchantUserId(@Param("userId") UUID userId, Pageable pageable);
}
