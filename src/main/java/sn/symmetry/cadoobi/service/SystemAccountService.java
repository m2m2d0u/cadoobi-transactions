package sn.symmetry.cadoobi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import sn.symmetry.cadoobi.domain.entity.PayoutTransaction;
import sn.symmetry.cadoobi.domain.entity.SystemAccount;
import sn.symmetry.cadoobi.domain.entity.SystemAccountEntry;
import sn.symmetry.cadoobi.domain.enums.LedgerDirection;
import sn.symmetry.cadoobi.domain.enums.SystemEntryType;
import sn.symmetry.cadoobi.dto.SystemAccountBalanceResponse;
import sn.symmetry.cadoobi.dto.SystemAccountEntryResponse;
import sn.symmetry.cadoobi.exception.ResourceNotFoundException;
import sn.symmetry.cadoobi.repository.SystemAccountEntryRepository;
import sn.symmetry.cadoobi.repository.SystemAccountRepository;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SystemAccountService {

    private final SystemAccountRepository systemAccountRepository;
    private final SystemAccountEntryRepository systemAccountEntryRepository;

    /**
     * Records merchant fee earned when a payout is completed.
     * Credits the system account with the fee amount collected.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordMerchantFeeEarned(PayoutTransaction tx, BigDecimal feeAmount) {
        String idempotencyKey = "MERCHANT_FEE_EARNED_" + tx.getId();
        if (systemAccountEntryRepository.existsByIdempotencyKey(idempotencyKey)) {
            log.warn("Duplicate MERCHANT_FEE_EARNED for payout {}, skipping", tx.getId());
            return;
        }

        if (feeAmount.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Fee amount is zero or negative for payout {}, skipping system account entry", tx.getId());
            return;
        }

        SystemAccount account = getOrCreateSystemAccount(tx.getCurrency());

        // Credit system account with fee earned
        account.setBalance(account.getBalance().add(feeAmount));
        systemAccountRepository.save(account);

        // Write system account entry
        writeSystemAccountEntry(account, LedgerDirection.CREDIT, SystemEntryType.MERCHANT_FEE_EARNED,
            feeAmount, tx.getCurrency(),
            "Merchant fee earned from payout: amount=" + feeAmount,
            idempotencyKey, tx);

        log.info("MERCHANT_FEE_EARNED posted: currency={}, amount={}, payoutId={}",
            tx.getCurrency(), feeAmount, tx.getId());
    }

    /**
     * Records a manual adjustment to the system account (admin operation).
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordManualAdjustment(String currency, LedgerDirection direction,
                                      BigDecimal amount, String description, String idempotencyKey) {
        if (systemAccountEntryRepository.existsByIdempotencyKey(idempotencyKey)) {
            log.warn("Duplicate manual adjustment with key {}, skipping", idempotencyKey);
            return;
        }

        SystemAccount account = getOrCreateSystemAccount(currency);

        // Apply adjustment based on direction
        if (direction == LedgerDirection.CREDIT) {
            account.setBalance(account.getBalance().add(amount));
        } else {
            account.setBalance(account.getBalance().subtract(amount));
        }
        systemAccountRepository.save(account);

        // Write system account entry
        writeSystemAccountEntry(account, direction, SystemEntryType.MANUAL_ADJUSTMENT,
            amount, currency, description, idempotencyKey, null);

        log.info("MANUAL_ADJUSTMENT posted: currency={}, direction={}, amount={}",
            currency, direction, amount);
    }

    // ── Query ─────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<SystemAccountBalanceResponse> getAllBalances() {
        return systemAccountRepository.findAll().stream()
            .map(this::toBalanceResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public SystemAccountBalanceResponse getBalance(String currency) {
        SystemAccount account = systemAccountRepository
            .findByCurrency(currency)
            .orElseThrow(() -> new ResourceNotFoundException(
                "No system account found for currency " + currency));
        return toBalanceResponse(account);
    }

    @Transactional(readOnly = true)
    public Page<SystemAccountEntryResponse> getEntries(String currency, Pageable pageable) {
        SystemAccount account = systemAccountRepository
            .findByCurrency(currency)
            .orElseThrow(() -> new ResourceNotFoundException(
                "No system account found for currency " + currency));
        return systemAccountEntryRepository.findBySystemAccountId(account.getId(), pageable)
            .map(this::toEntryResponse);
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    private SystemAccount getOrCreateSystemAccount(String currency) {
        return systemAccountRepository
            .findByCurrency(currency)
            .orElseGet(() -> {
                SystemAccount account = SystemAccount.builder()
                    .currency(currency)
                    .balance(BigDecimal.ZERO)
                    .build();
                return systemAccountRepository.save(account);
            });
    }

    private void writeSystemAccountEntry(SystemAccount account, LedgerDirection direction,
                                         SystemEntryType type, BigDecimal amount, String currency,
                                         String description, String idempotencyKey,
                                         PayoutTransaction payoutTx) {
        SystemAccountEntry entry = SystemAccountEntry.builder()
            .systemAccount(account)
            .direction(direction)
            .entryType(type)
            .amount(amount)
            .currency(currency)
            .description(description)
            .idempotencyKey(idempotencyKey)
            .payoutTransaction(payoutTx)
            .build();
        systemAccountEntryRepository.save(entry);
    }

    private SystemAccountBalanceResponse toBalanceResponse(SystemAccount account) {
        return SystemAccountBalanceResponse.builder()
            .accountId(account.getId())
            .currency(account.getCurrency())
            .balance(account.getBalance())
            .updatedAt(account.getUpdatedAt())
            .build();
    }

    private SystemAccountEntryResponse toEntryResponse(SystemAccountEntry entry) {
        return SystemAccountEntryResponse.builder()
            .id(entry.getId())
            .systemAccountId(entry.getSystemAccount().getId())
            .direction(entry.getDirection())
            .entryType(entry.getEntryType())
            .amount(entry.getAmount())
            .currency(entry.getCurrency())
            .description(entry.getDescription())
            .idempotencyKey(entry.getIdempotencyKey())
            .payoutTransactionId(entry.getPayoutTransaction() != null
                ? entry.getPayoutTransaction().getId() : null)
            .createdAt(entry.getCreatedAt())
            .build();
    }
}
