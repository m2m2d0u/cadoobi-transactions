package sn.symmetry.cadoobi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.symmetry.cadoobi.domain.entity.Operator;
import sn.symmetry.cadoobi.domain.entity.PaymentTransaction;
import sn.symmetry.cadoobi.domain.enums.OperationType;
import sn.symmetry.cadoobi.domain.enums.PaymentStatus;
import sn.symmetry.cadoobi.dto.InitiatePaymentRequest;
import sn.symmetry.cadoobi.dto.PaymentResponse;
import sn.symmetry.cadoobi.exception.DuplicateResourceException;
import sn.symmetry.cadoobi.exception.ResourceNotFoundException;
import sn.symmetry.cadoobi.repository.PaymentTransactionRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentTransactionRepository paymentTransactionRepository;
    private final OperatorService operatorService;
    private final OperatorFeeService operatorFeeService;

    private static final long PAYMENT_EXPIRY_HOURS = 24;

    @Transactional
    public PaymentResponse initiatePayment(InitiatePaymentRequest request) {
        if (paymentTransactionRepository.existsByReference(request.getReference())) {
            throw new DuplicateResourceException("Payment with reference " + request.getReference() + " already exists");
        }

        Operator operator = operatorService.getOperatorByCode(request.getOperatorCode());

        if (!operator.getSupportsPayin()) {
            throw new IllegalArgumentException("Operator " + operator.getName() + " does not support PAYIN operations");
        }

        BigDecimal feeAmount = operatorFeeService.calculateFee(
            operator.getId(),
            OperationType.PAYIN,
            request.getAmount()
        );

        BigDecimal netAmount = request.getAmount().subtract(feeAmount);

        PaymentTransaction payment = PaymentTransaction.builder()
            .reference(request.getReference())
            .merchantId(request.getMerchantId())
            .merchantCode(request.getMerchantCode())
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
            payment.getReference(), payment.getMerchantId(), operator.getCode(),
            payment.getAmount(), payment.getFeeAmount());

        return toResponse(payment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByReference(String reference) {
        PaymentTransaction payment = paymentTransactionRepository.findByReference(reference)
            .orElseThrow(() -> new ResourceNotFoundException("Payment not found with reference: " + reference));
        return toResponse(payment);
    }

    @Transactional
    public PaymentTransaction updatePaymentStatus(String reference, PaymentStatus status, String operatorTransactionId) {
        PaymentTransaction payment = paymentTransactionRepository.findByReference(reference)
            .orElseThrow(() -> new ResourceNotFoundException("Payment not found with reference: " + reference));

        payment.setStatus(status);
        if (operatorTransactionId != null) {
            payment.setOperatorTransactionId(operatorTransactionId);
        }

        payment = paymentTransactionRepository.save(payment);

        log.info("Updated payment status: reference={}, status={}, operatorTxnId={}",
            reference, status, operatorTransactionId);

        return payment;
    }

    private PaymentResponse toResponse(PaymentTransaction payment) {
        return PaymentResponse.builder()
            .id(payment.getId())
            .reference(payment.getReference())
            .merchantId(payment.getMerchantId())
            .merchantCode(payment.getMerchantCode())
            .operatorCode(payment.getOperator().getCode())
            .amount(payment.getAmount())
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
