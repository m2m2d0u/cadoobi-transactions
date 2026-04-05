package sn.symmetry.cadoobi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.symmetry.cadoobi.domain.entity.Merchant;
import sn.symmetry.cadoobi.domain.entity.Operator;
import sn.symmetry.cadoobi.domain.entity.PayoutTransaction;
import sn.symmetry.cadoobi.domain.enums.OperationType;
import sn.symmetry.cadoobi.domain.enums.PayoutStatus;
import sn.symmetry.cadoobi.dto.CreatePayoutRequest;
import sn.symmetry.cadoobi.dto.PayoutResponse;
import sn.symmetry.cadoobi.event.PayoutStatusChangedEvent;
import sn.symmetry.cadoobi.exception.BusinessException;
import sn.symmetry.cadoobi.exception.ResourceNotFoundException;
import sn.symmetry.cadoobi.repository.MerchantRepository;
import sn.symmetry.cadoobi.repository.PayoutTransactionRepository;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayoutService {

    private final PayoutTransactionRepository payoutRepository;
    private final MerchantRepository merchantRepository;
    private final OperatorService operatorService;
    private final OperatorFeeService operatorFeeService;
    private final LedgerService ledgerService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public PayoutResponse createPayout(CreatePayoutRequest request) {
        if (payoutRepository.existsByIdempotencyKey(request.getIdempotencyKey())) {
            return toResponse(payoutRepository.findByIdempotencyKey(request.getIdempotencyKey()).orElseThrow());
        }

        Operator operator = operatorService.getOperatorByCode(request.getOperatorCode());
        if (!operator.getSupportsPayout()) {
            throw new BusinessException("Operator " + operator.getName() + " does not support PAYOUT operations");
        }

        BigDecimal operatorFee = operatorFeeService.calculateFee(
                operator.getId(), OperationType.PAYOUT, request.getAmount());
        BigDecimal netAmount = request.getAmount().subtract(operatorFee);

        PayoutTransaction payout = PayoutTransaction.builder()
                .merchantId(request.getMerchantId())
                .operator(operator)
                .recipientNumber(request.getRecipientNumber())
                .amount(request.getAmount())
                .feeAmount(operatorFee)
                .netAmount(netAmount)
                .currency(request.getCurrency())
                .status(PayoutStatus.PENDING)
                .idempotencyKey(request.getIdempotencyKey())
                .merchantFeeAmount(BigDecimal.ZERO)  // Initialize to ZERO, will be updated after calculation
                .build();

        payout = payoutRepository.save(payout);

        BigDecimal merchantFee = ledgerService.calculateAndLockForPayout(payout);
        payout.setMerchantFeeAmount(merchantFee);
        payout = payoutRepository.save(payout);

        log.info("Created payout: id={}, merchant={}, amount={}, merchantFee={}",
                payout.getId(), payout.getMerchantId(), payout.getAmount(), merchantFee);

        return toResponse(payout);
    }

    @Transactional(readOnly = true)
    public PayoutResponse getPayoutById(UUID id) {
        return toResponse(findById(id));
    }

    @Transactional(readOnly = true)
    public Page<PayoutResponse> getPayoutsByMerchant(String merchantId, Pageable pageable) {
        return payoutRepository.findByMerchantId(merchantId, pageable).map(this::toResponse);
    }

    /**
     * Get all payout transactions with role-based access control.
     * - SUPER_ADMIN and ADMIN can view all payouts
     * - Other users can only view payouts for their own merchant accounts
     */
    @Transactional(readOnly = true)
    public Page<PayoutResponse> getAllPayouts(UUID currentUserId, boolean isAdmin, Pageable pageable) {
        if (isAdmin) {
            // Admin users can see all payout transactions
            return payoutRepository.findAll(pageable).map(this::toResponse);
        } else {
            // Regular users can only see payouts for merchants they manage
            return payoutRepository.findByMerchantUserId(currentUserId, pageable)
                    .map(this::toResponse);
        }
    }

    @Transactional
    public PayoutResponse executePayout(UUID id) {
        PayoutTransaction payout = findById(id);

        // Validate payout is in PENDING status
        if (payout.getStatus() != PayoutStatus.PENDING) {
            throw new BusinessException("Cannot execute payout in status: " + payout.getStatus() +
                ". Only PENDING payouts can be executed.");
        }

        PayoutStatus oldStatus = payout.getStatus();

        // TODO: Integrate with operator API to initiate the withdrawal
        // This is where you would call the operator's payout/withdrawal API
        // Example:
        // OperatorPayoutResponse operatorResponse = operatorService.initiateWithdrawal(payout);
        // payout.setOperatorTransactionId(operatorResponse.getTransactionId());

        // Update status to PROCESSING
        payout.setStatus(PayoutStatus.PROCESSING);
        payout = payoutRepository.save(payout);

        log.info("Executed payout: id={}, merchant={}, amount={}, operator={}",
            payout.getId(), payout.getMerchantId(), payout.getAmount(), payout.getOperator().getCode());

        // Publish event
        eventPublisher.publishEvent(new PayoutStatusChangedEvent(this, payout, oldStatus, PayoutStatus.PROCESSING));

        return toResponse(payout);
    }

    @Transactional
    public PayoutResponse updatePayoutStatus(UUID id, PayoutStatus newStatus, String operatorTransactionId) {
        PayoutTransaction payout = findById(id);
        PayoutStatus oldStatus = payout.getStatus();

        if (oldStatus == newStatus) {
            return toResponse(payout);
        }

        payout.setStatus(newStatus);
        if (operatorTransactionId != null) {
            payout.setOperatorTransactionId(operatorTransactionId);
        }

        payout = payoutRepository.save(payout);
        log.info("Updated payout status: id={}, {} -> {}", id, oldStatus, newStatus);

        eventPublisher.publishEvent(new PayoutStatusChangedEvent(this, payout, oldStatus, newStatus));

        return toResponse(payout);
    }

    // ── Internal ──────────────────────────────────────────────────────────────

    public PayoutTransaction findById(UUID id) {
        return payoutRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payout not found with id: " + id));
    }

    private PayoutResponse toResponse(PayoutTransaction payout) {
        // Look up merchant to get the code
        String merchantCode = resolveMerchantCode(payout.getMerchantId());

        return PayoutResponse.builder()
                .id(payout.getId())
                .merchantId(payout.getMerchantId())
                .merchantCode(merchantCode)
                .operatorCode(payout.getOperator().getCode())
                .recipientNumber(payout.getRecipientNumber())
                .amount(payout.getAmount())
                .feeAmount(payout.getFeeAmount())
                .netAmount(payout.getNetAmount())
                .merchantFeeAmount(payout.getMerchantFeeAmount())
                .currency(payout.getCurrency())
                .status(payout.getStatus())
                .idempotencyKey(payout.getIdempotencyKey())
                .operatorTransactionId(payout.getOperatorTransactionId())
                .createdAt(payout.getCreatedAt())
                .updatedAt(payout.getUpdatedAt())
                .build();
    }

    private String resolveMerchantCode(String merchantIdOrSymmetryId) {
        // Try to parse as UUID first (internal merchant ID)
        try {
            UUID merchantUuid = UUID.fromString(merchantIdOrSymmetryId);
            return merchantRepository.findById(merchantUuid)
                .map(Merchant::getCode)
                .orElse(null);
        } catch (IllegalArgumentException e) {
            // Not a valid UUID, try as symmetryMerchantId
            return merchantRepository.findBySymmetryMerchantId(merchantIdOrSymmetryId)
                .map(Merchant::getCode)
                .orElse(null);
        }
    }
}
