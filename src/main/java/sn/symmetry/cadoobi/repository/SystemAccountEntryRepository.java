package sn.symmetry.cadoobi.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sn.symmetry.cadoobi.domain.entity.SystemAccountEntry;
import sn.symmetry.cadoobi.domain.enums.LedgerDirection;
import sn.symmetry.cadoobi.domain.enums.SystemEntryType;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SystemAccountEntryRepository extends JpaRepository<SystemAccountEntry, UUID> {

    boolean existsByIdempotencyKey(String idempotencyKey);

    Optional<SystemAccountEntry> findByIdempotencyKey(String idempotencyKey);

    Page<SystemAccountEntry> findBySystemAccountId(UUID systemAccountId, Pageable pageable);

    Page<SystemAccountEntry> findBySystemAccountIdAndDirection(UUID systemAccountId, LedgerDirection direction, Pageable pageable);

    Page<SystemAccountEntry> findBySystemAccountIdAndEntryType(UUID systemAccountId, SystemEntryType entryType, Pageable pageable);

    Page<SystemAccountEntry> findBySystemAccountIdAndCreatedAtBetween(UUID systemAccountId, Instant from, Instant to, Pageable pageable);
}
