package sn.symmetry.cadoobi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.symmetry.cadoobi.domain.entity.Merchant;
import sn.symmetry.cadoobi.domain.entity.Operator;
import sn.symmetry.cadoobi.domain.entity.OperatorCallback;
import sn.symmetry.cadoobi.domain.entity.PaymentTransaction;
import sn.symmetry.cadoobi.domain.enums.NotificationEventType;
import sn.symmetry.cadoobi.domain.enums.OperationType;
import sn.symmetry.cadoobi.domain.enums.PaymentStatus;
import sn.symmetry.cadoobi.dto.InitiatePaymentRequest;
import sn.symmetry.cadoobi.dto.OperatorCallbackRequest;
import sn.symmetry.cadoobi.dto.PaymentResponse;
import sn.symmetry.cadoobi.event.PaymentCompletedEvent;
import sn.symmetry.cadoobi.exception.BusinessException;
import sn.symmetry.cadoobi.exception.DuplicateResourceException;
import sn.symmetry.cadoobi.exception.ResourceNotFoundException;
import sn.symmetry.cadoobi.repository.OperatorCallbackRepository;
import sn.symmetry.cadoobi.repository.PaymentTransactionRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentTransactionRepository paymentTransactionRepository;
    private final OperatorCallbackRepository operatorCallbackRepository;
    private final OperatorService operatorService;
    private final OperatorFeeService operatorFeeService;
    private final MerchantService merchantService;
    private final NotificationService notificationService;
    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    private static final long PAYMENT_EXPIRY_HOURS = 24;

    @Transactional
    public PaymentResponse initiatePayment(InitiatePaymentRequest request, UUID userId) {
        Merchant merchant = merchantService.findByCode(request.getMerchantCode());
        Operator operator = operatorService.getOperatorByCode(request.getOperatorCode());

        if (!operator.getSupportsPayin()) {
            throw new IllegalArgumentException("Operator " + operator.getName() + " does not support PAYIN operations");
        }

        if (!merchant.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Merchant does not belong to user");
        }

        BigDecimal feeAmount = operatorFeeService.calculateFee(
                operator.getId(),
                OperationType.PAYIN,
                request.getAmount()
        );

        Optional<PaymentTransaction> paymentTransaction = paymentTransactionRepository.findByReference(request.getReference());

        if (paymentTransaction.isPresent()) {
            throw new DuplicateResourceException("Payment already initiated with reference: " + request.getReference());
        }

        BigDecimal netAmount = request.getAmount().subtract(feeAmount);
//        String reference = generateReference(merchant.getCode());

        PaymentTransaction payment = PaymentTransaction.builder()
                .reference(request.getReference())
                .merchant(merchant)
                .operator(operator)
                .amount(request.getAmount())
                .feeAmount(feeAmount)
                .netAmount(netAmount)
                .currency(request.getCurrency())
                .payerPhone(request.getPayerPhone())
                .payerFullName(request.getPayerFullName())
                .recipientPhone(request.getRecipientPhone())
                .recipientName(request.getRecipientName())
                .status(PaymentStatus.INITIATED)
                .callbackUrl(request.getCallbackUrl())
                .expiresAt(Instant.now().plus(PAYMENT_EXPIRY_HOURS, ChronoUnit.HOURS))
                .build();

        payment = paymentTransactionRepository.save(payment);

        log.info("Initiated payment: reference={}, merchant={}, operator={}, amount={}, fee={}",
                payment.getReference(), merchant.getCode(), operator.getCode(),
                payment.getAmount(), payment.getFeeAmount());

        return toResponse(payment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByReference(String reference) {
        PaymentTransaction payment = paymentTransactionRepository.findByReference(reference)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with reference: " + reference));
        return toResponse(payment);
    }

    /**
     * Get all payment transactions with role-based access control.
     * - SUPER_ADMIN and ADMIN can view all transactions
     * - Other users can only view transactions for their own merchant accounts
     */
    @Transactional(readOnly = true)
    public Page<PaymentResponse> getAllTransactions(UUID currentUserId, boolean isAdmin, Pageable pageable) {
        if (isAdmin) {
            // Admin users can see all payment transactions
            return paymentTransactionRepository.findAll(pageable)
                    .map(this::toResponse);
        } else {
            // Regular users can only see transactions for merchants they manage
            return paymentTransactionRepository.findByMerchantUserId(currentUserId, pageable)
                    .map(this::toResponse);
        }
    }

    @Transactional
    public void handleOperatorCallback(String operatorCode, OperatorCallbackRequest request) {
        log.info("Processing operator callback: operator={}, reference={}, status={}",
                operatorCode, request.getPaymentReference(), request.getStatus());

        // 1. Validate operator
        Operator operator = operatorService.getOperatorByCode(operatorCode);

        // 2. Check for duplicate callback
        if (operatorCallbackRepository.existsByOperatorReference(request.getOperatorReference())) {
            log.warn("Duplicate callback received: operatorReference={}", request.getOperatorReference());
            throw new DuplicateResourceException("Callback already processed for operator reference: " + request.getOperatorReference());
        }

        // 3. Find payment transaction
        PaymentTransaction payment = paymentTransactionRepository.findByReference(request.getPaymentReference())
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with reference: " + request.getPaymentReference()));

        // 4. Validate payment is not in final state
        if (payment.getStatus() == PaymentStatus.COMPLETED ||
                payment.getStatus() == PaymentStatus.CANCELLED ||
                payment.getStatus() == PaymentStatus.EXPIRED) {
            log.warn("Payment already in final state: reference={}, currentStatus={}",
                    payment.getReference(), payment.getStatus());
            throw new BusinessException("Payment already in final state: " + payment.getStatus());
        }

        // 5. Map operator status to our PaymentStatus
        PaymentStatus newStatus = mapOperatorStatusToPaymentStatus(request.getStatus());

        // 6. Store callback record
        OperatorCallback callback = OperatorCallback.builder()
                .paymentTransaction(payment)
                .operator(operator)
                .operatorReference(request.getOperatorReference())
                .rawPayload(request.getRawPayload() != null ? request.getRawPayload() : "")
                .operatorStatus(request.getStatus())
                .processedAt(Instant.now())
                .build();
        operatorCallbackRepository.save(callback);

        // 7. Update payment status
        updatePaymentStatus(payment.getReference(), newStatus, request.getOperatorTransactionId());

        // 8. Send notification to merchant callback URL
        sendMerchantCallback(payment, newStatus);

        log.info("Operator callback processed successfully: reference={}, newStatus={}",
                payment.getReference(), newStatus);
    }

    @Transactional
    public PaymentTransaction updatePaymentStatus(String reference, PaymentStatus status, String operatorTransactionId) {
        PaymentTransaction payment = paymentTransactionRepository.findByReference(reference)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with reference: " + reference));

        PaymentStatus oldStatus = payment.getStatus();
        payment.setStatus(status);

        if (operatorTransactionId != null && !operatorTransactionId.isBlank()) {
            payment.setOperatorTransactionId(operatorTransactionId);
        }

        payment = paymentTransactionRepository.save(payment);

        log.info("Updated payment status: reference={}, oldStatus={}, newStatus={}, operatorTxnId={}",
                reference, oldStatus, status, operatorTransactionId);

        // Publish event for completed payments to trigger ledger settlement
        if (status == PaymentStatus.COMPLETED) {
            eventPublisher.publishEvent(new PaymentCompletedEvent(this, payment));
        }

        return payment;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String generateReference(String merchantCode) {
        String date = LocalDate.now(ZoneOffset.UTC).format(DateTimeFormatter.BASIC_ISO_DATE);
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return merchantCode + "-" + date + "-" + suffix;
    }

    /**
     * Maps operator-specific status strings to our internal PaymentStatus enum.
     * Customize this method based on your operators' status conventions.
     */
    private PaymentStatus mapOperatorStatusToPaymentStatus(String operatorStatus) {
        if (operatorStatus == null || operatorStatus.isBlank()) {
            return PaymentStatus.PENDING;
        }

        String status = operatorStatus.toUpperCase().trim();

        // Map common success variations
        if (status.contains("SUCCESS") || status.contains("COMPLETED") ||
                status.contains("CONFIRMED") || status.equals("OK")) {
            return PaymentStatus.COMPLETED;
        }

        // Map common failure variations
        if (status.contains("FAIL") || status.contains("REJECT") ||
                status.contains("DECLINE") || status.contains("ERROR")) {
            return PaymentStatus.FAILED;
        }

        // Map cancelled variations
        if (status.contains("CANCEL") || status.contains("ABORT")) {
            return PaymentStatus.CANCELLED;
        }

        // Map expired variations
        if (status.contains("EXPIRE") || status.contains("TIMEOUT")) {
            return PaymentStatus.EXPIRED;
        }

        // Default to PENDING for unknown or in-progress statuses
        if (status.contains("PENDING") || status.contains("PROCESS") ||
                status.contains("ONGOING") || status.contains("PROGRESS")) {
            return PaymentStatus.PENDING;
        }

        // If we can't map it, log a warning and default to PENDING
        log.warn("Unknown operator status: '{}', defaulting to PENDING", operatorStatus);
        return PaymentStatus.PENDING;
    }

    /**
     * Sends payment status notification to merchant's callback URL
     */
    private void sendMerchantCallback(PaymentTransaction payment, PaymentStatus status) {
        try {
            String callbackUrl = payment.getCallbackUrl();
            if (callbackUrl == null || callbackUrl.isBlank()) {
                log.warn("No callback URL configured for payment: {}", payment.getReference());
                return;
            }

            // Build callback payload
            PaymentResponse paymentResponse = toResponse(payment);
            String payload = objectMapper.writeValueAsString(paymentResponse);

            // Send async notification
            notificationService.sendNotification(
                    NotificationEventType.PAYMENT_STATUS_UPDATE,
                    callbackUrl,
                    payload
            );

            log.info("Merchant callback queued: reference={}, url={}", payment.getReference(), callbackUrl);

        } catch (Exception e) {
            log.error("Failed to queue merchant callback for payment: {}", payment.getReference(), e);
            // Don't throw - callback failure shouldn't prevent payment processing
        }
    }

    private PaymentResponse toResponse(PaymentTransaction payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .reference(payment.getReference())
                .merchantCode(payment.getMerchant().getCode())
                .merchantId(payment.getMerchant().getSymmetryMerchantId())
                .operatorCode(payment.getOperator().getCode())
                .amount(payment.getAmount())
                .payerPhone(payment.getPayerPhone())
                .feeAmount(payment.getFeeAmount())
                .netAmount(payment.getNetAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus())
                .operatorTransactionId(payment.getOperatorTransactionId())
                .paymentUrl(payment.getPaymentUrl())
                .expiresAt(payment.getExpiresAt())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}
