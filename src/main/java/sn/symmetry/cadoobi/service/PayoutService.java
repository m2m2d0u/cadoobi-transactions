package sn.symmetry.cadoobi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.symmetry.cadoobi.domain.entity.Operator;
import sn.symmetry.cadoobi.domain.entity.PayoutTransaction;
import sn.symmetry.cadoobi.domain.enums.OperationType;
import sn.symmetry.cadoobi.domain.enums.PayoutStatus;
import sn.symmetry.cadoobi.dto.CreatePayoutRequest;
import sn.symmetry.cadoobi.dto.PayoutResponse;
import sn.symmetry.cadoobi.event.PayoutStatusChangedEvent;
import sn.symmetry.cadoobi.exception.BusinessException;
import sn.symmetry.cadoobi.exception.ResourceNotFoundException;
import sn.symmetry.cadoobi.repository.PayoutTransactionRepository;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayoutService {

    private final PayoutTransactionRepository payoutRepository;
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
        return PayoutResponse.builder()
            .id(payout.getId())
            .merchantId(payout.getMerchantId())
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
}
