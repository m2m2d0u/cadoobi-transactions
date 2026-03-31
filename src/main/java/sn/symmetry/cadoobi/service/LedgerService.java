package sn.symmetry.cadoobi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import sn.symmetry.cadoobi.domain.entity.LedgerEntry;
import sn.symmetry.cadoobi.domain.entity.MerchantAccount;
import sn.symmetry.cadoobi.domain.entity.PaymentTransaction;
import sn.symmetry.cadoobi.domain.entity.PayoutTransaction;
import sn.symmetry.cadoobi.domain.enums.LedgerDirection;
import sn.symmetry.cadoobi.domain.enums.LedgerEntryType;
import sn.symmetry.cadoobi.dto.LedgerEntryResponse;
import sn.symmetry.cadoobi.dto.MerchantBalanceResponse;
import sn.symmetry.cadoobi.exception.BusinessException;
import sn.symmetry.cadoobi.exception.ResourceNotFoundException;
import sn.symmetry.cadoobi.repository.LedgerEntryRepository;
import sn.symmetry.cadoobi.repository.MerchantAccountRepository;
import sn.symmetry.cadoobi.repository.MerchantRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class LedgerService {

    private final MerchantAccountRepository merchantAccountRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final MerchantRepository merchantRepository;
    private final MerchantFeeService merchantFeeService;
    private final SystemAccountService systemAccountService;

    // ── PAYIN ─────────────────────────────────────────────────────────────────

    /**
     * Credits the merchant's ledger with the net amount received from the PSP.
     * Called after a PaymentTransaction is confirmed COMPLETED.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void postPayinSettlement(PaymentTransaction tx) {
        String idempotencyKey = "PAYIN_SETTLEMENT_" + tx.getId();
        if (ledgerEntryRepository.existsByIdempotencyKey(idempotencyKey)) {
            log.warn("Duplicate PAYIN_SETTLEMENT for payment {}, skipping", tx.getId());
            return;
        }

        MerchantAccount account = getOrCreateAccount(tx.getMerchant().getId(), tx.getCurrency());
        BigDecimal netAmount = tx.getNetAmount();

        account.setBalance(account.getBalance().add(netAmount));
        merchantAccountRepository.save(account);

        writeLedgerEntry(account, LedgerDirection.CREDIT, LedgerEntryType.PAYIN_SETTLEMENT,
            netAmount, tx.getCurrency(),
            "PAYIN settled: ref=" + tx.getReference(),
            idempotencyKey, tx, null);

        log.info("PAYIN_SETTLEMENT posted: merchant={}, amount={}, ref={}",
            tx.getMerchant().getCode(), netAmount, tx.getReference());
    }

    // ── PAYOUT ────────────────────────────────────────────────────────────────

    /**
     * Reserves funds for a pending payout.
     * Checks available balance before locking; throws BusinessException if insufficient.
     * Sets merchantFeeAmount on the PayoutTransaction before it is persisted.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BigDecimal calculateAndLockForPayout(PayoutTransaction tx) {
        String idempotencyKey = "PAYOUT_LOCK_" + tx.getId();
        if (ledgerEntryRepository.existsByIdempotencyKey(idempotencyKey)) {
            log.warn("Duplicate PAYOUT_LOCK for payout {}, skipping", tx.getId());
            return tx.getMerchantFeeAmount();
        }

        UUID merchantId = resolveMerchantId(tx.getMerchantId());
        MerchantAccount account = getOrCreateAccount(merchantId, tx.getCurrency());
        BigDecimal feeAmount = merchantFeeService.calculateFee(merchantId, tx.getAmount());
        BigDecimal totalCharge = tx.getAmount().add(feeAmount);

        if (account.getAvailableBalance().compareTo(totalCharge) < 0) {
            throw new BusinessException(
                "Insufficient balance. Available: " + account.getAvailableBalance() +
                ", Required: " + totalCharge
            );
        }

        account.setLockedBalance(account.getLockedBalance().add(totalCharge));
        merchantAccountRepository.save(account);

        writeLedgerEntry(account, LedgerDirection.DEBIT, LedgerEntryType.PAYOUT_LOCK,
            totalCharge, tx.getCurrency(),
            "PAYOUT locked: amount=" + tx.getAmount() + " + fee=" + feeAmount,
            idempotencyKey, null, tx);

        log.info("PAYOUT_LOCK posted: merchant={}, amount={}, fee={}, total={}",
            tx.getMerchantId(), tx.getAmount(), feeAmount, totalCharge);

        return feeAmount;
    }

    /**
     * Releases a payout lock when the payout is FAILED or CANCELLED.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void releasePayoutLock(PayoutTransaction tx) {
        String idempotencyKey = "PAYOUT_RELEASE_" + tx.getId();
        if (ledgerEntryRepository.existsByIdempotencyKey(idempotencyKey)) {
            log.warn("Duplicate PAYOUT_RELEASE for payout {}, skipping", tx.getId());
            return;
        }

        UUID merchantId = resolveMerchantId(tx.getMerchantId());
        MerchantAccount account = getOrCreateAccount(merchantId, tx.getCurrency());
        BigDecimal totalCharge = tx.getAmount().add(tx.getMerchantFeeAmount());

        account.setLockedBalance(account.getLockedBalance().subtract(totalCharge));
        merchantAccountRepository.save(account);

        writeLedgerEntry(account, LedgerDirection.CREDIT, LedgerEntryType.PAYOUT_RELEASE,
            totalCharge, tx.getCurrency(),
            "PAYOUT released (failed/cancelled): amount=" + tx.getAmount() + " + fee=" + tx.getMerchantFeeAmount(),
            idempotencyKey, null, tx);

        log.info("PAYOUT_RELEASE posted: merchant={}, total={}", tx.getMerchantId(), totalCharge);
    }

    /**
     * Settles a completed payout: debits the declared amount and the merchant fee.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void settleCompletedPayout(PayoutTransaction tx) {
        String settlementKey = "PAYOUT_SETTLEMENT_" + tx.getId();
        if (ledgerEntryRepository.existsByIdempotencyKey(settlementKey)) {
            log.warn("Duplicate PAYOUT_SETTLEMENT for payout {}, skipping", tx.getId());
            return;
        }

        UUID merchantId = resolveMerchantId(tx.getMerchantId());
        MerchantAccount account = getOrCreateAccount(merchantId, tx.getCurrency());
        BigDecimal feeAmount = tx.getMerchantFeeAmount();
        BigDecimal totalCharge = tx.getAmount().add(feeAmount);

        // Debit total from balance, release the full lock
        account.setBalance(account.getBalance().subtract(totalCharge));
        account.setLockedBalance(account.getLockedBalance().subtract(totalCharge));
        merchantAccountRepository.save(account);

        // Main settlement entry: cash going out to PSP
        writeLedgerEntry(account, LedgerDirection.DEBIT, LedgerEntryType.PAYOUT_SETTLEMENT,
            tx.getAmount(), tx.getCurrency(),
            "PAYOUT settled: amount=" + tx.getAmount(),
            settlementKey, null, tx);

        // Fee sidecar: Cadoobi revenue (balance already debited above)
        if (feeAmount.compareTo(BigDecimal.ZERO) > 0) {
            writeLedgerEntry(account, LedgerDirection.DEBIT, LedgerEntryType.PAYOUT_FEE,
                feeAmount, tx.getCurrency(),
                "PAYOUT fee retained: " + feeAmount,
                "PAYOUT_FEE_" + tx.getId(), null, tx);

            // Record fee earned in system account
            systemAccountService.recordMerchantFeeEarned(tx, feeAmount);
        }

        log.info("PAYOUT_SETTLEMENT posted: merchant={}, amount={}, fee={}",
            tx.getMerchantId(), tx.getAmount(), feeAmount);
    }

    // ── Query ─────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<MerchantBalanceResponse> getAllBalances(UUID merchantId) {
        return merchantAccountRepository.findByMerchantId(merchantId).stream()
            .map(this::toBalanceResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public MerchantBalanceResponse getBalance(UUID merchantId, String currency) {
        MerchantAccount account = merchantAccountRepository
            .findByMerchantIdAndCurrency(merchantId, currency)
            .orElseThrow(() -> new ResourceNotFoundException(
                "No ledger account found for merchant " + merchantId + " currency " + currency));
        return toBalanceResponse(account);
    }

    @Transactional(readOnly = true)
    public Page<LedgerEntryResponse> getEntries(UUID merchantId, String currency, Pageable pageable) {
        MerchantAccount account = merchantAccountRepository
            .findByMerchantIdAndCurrency(merchantId, currency)
            .orElseThrow(() -> new ResourceNotFoundException(
                "No ledger account found for merchant " + merchantId + " currency " + currency));
        return ledgerEntryRepository.findByMerchantAccountId(account.getId(), pageable)
            .map(this::toEntryResponse);
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    public MerchantAccount getOrCreateAccount(UUID merchantId, String currency) {
        return merchantAccountRepository
            .findByMerchantIdAndCurrency(merchantId, currency)
            .orElseGet(() -> {
                var merchant = merchantRepository.findById(merchantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Merchant not found: " + merchantId));
                MerchantAccount account = MerchantAccount.builder()
                    .merchant(merchant)
                    .currency(currency)
                    .balance(BigDecimal.ZERO)
                    .lockedBalance(BigDecimal.ZERO)
                    .build();
                return merchantAccountRepository.save(account);
            });
    }

    private UUID resolveMerchantId(String symmetryMerchantId) {
        return merchantRepository.findBySymmetryMerchantId(symmetryMerchantId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Merchant not found with symmetryMerchantId: " + symmetryMerchantId))
            .getId();
    }

    private void writeLedgerEntry(MerchantAccount account, LedgerDirection direction,
                                   LedgerEntryType type, BigDecimal amount, String currency,
                                   String description, String idempotencyKey,
                                   PaymentTransaction paymentTx, PayoutTransaction payoutTx) {
        LedgerEntry entry = LedgerEntry.builder()
            .merchantAccount(account)
            .direction(direction)
            .entryType(type)
            .amount(amount)
            .currency(currency)
            .description(description)
            .idempotencyKey(idempotencyKey)
            .paymentTransaction(paymentTx)
            .payoutTransaction(payoutTx)
            .build();
        ledgerEntryRepository.save(entry);
    }

    private MerchantBalanceResponse toBalanceResponse(MerchantAccount account) {
        return MerchantBalanceResponse.builder()
            .accountId(account.getId())
            .merchantId(account.getMerchant().getId())
            .currency(account.getCurrency())
            .balance(account.getBalance())
            .lockedBalance(account.getLockedBalance())
            .availableBalance(account.getAvailableBalance())
            .updatedAt(account.getUpdatedAt())
            .build();
    }

    private LedgerEntryResponse toEntryResponse(LedgerEntry entry) {
        return LedgerEntryResponse.builder()
            .id(entry.getId())
            .merchantAccountId(entry.getMerchantAccount().getId())
            .direction(entry.getDirection())
            .entryType(entry.getEntryType())
            .amount(entry.getAmount())
            .currency(entry.getCurrency())
            .description(entry.getDescription())
            .idempotencyKey(entry.getIdempotencyKey())
            .paymentTransactionId(entry.getPaymentTransaction() != null
                ? entry.getPaymentTransaction().getId() : null)
            .payoutTransactionId(entry.getPayoutTransaction() != null
                ? entry.getPayoutTransaction().getId() : null)
            .createdAt(entry.getCreatedAt())
            .build();
    }
}
