package sn.symmetry.cadoobi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.symmetry.cadoobi.domain.entity.GiftCard;
import sn.symmetry.cadoobi.domain.entity.GiftCardRedemption;
import sn.symmetry.cadoobi.domain.entity.PaymentTransaction;
import sn.symmetry.cadoobi.domain.entity.PayoutTransaction;
import sn.symmetry.cadoobi.domain.enums.CardStatus;
import sn.symmetry.cadoobi.domain.enums.OperationType;
import sn.symmetry.cadoobi.domain.enums.PayoutStatus;
import sn.symmetry.cadoobi.domain.enums.RedemptionStatus;
import sn.symmetry.cadoobi.dto.GiftCardBalanceResponse;
import sn.symmetry.cadoobi.dto.RedeemGiftCardRequest;
import sn.symmetry.cadoobi.dto.RedemptionResponse;
import sn.symmetry.cadoobi.exception.BusinessException;
import sn.symmetry.cadoobi.exception.DuplicateResourceException;
import sn.symmetry.cadoobi.exception.ResourceNotFoundException;
import sn.symmetry.cadoobi.repository.*;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class GiftCardService {

    private final GiftCardRepository giftCardRepository;
    private final GiftCardRedemptionRepository redemptionRepository;
    private final PayoutTransactionRepository payoutRepository;
    private final OperatorFeeService operatorFeeService;
    private final OperatorRepository operatorRepository;
    private final QRCodeService qrCodeService;

    private static final String CARD_CODE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CARD_CODE_LENGTH = 12;
    private static final long CARD_EXPIRY_DAYS = 365;

    @Transactional
    public GiftCard createGiftCard(PaymentTransaction payment) {
        if (giftCardRepository.findByPaymentTransactionId(payment.getId()).isPresent()) {
            throw new DuplicateResourceException("Gift card already exists for payment: " + payment.getReference());
        }

        String cardCode = generateUniqueCardCode();
        String qrCodeData = qrCodeService.generateQRCodeBase64(cardCode);

        GiftCard giftCard = GiftCard.builder()
            .paymentTransaction(payment)
            .merchantId(payment.getMerchant().getSymmetryMerchantId())
            .cardCode(cardCode)
            .qrCodeData(qrCodeData)
            .initialAmount(payment.getNetAmount())
            .balance(payment.getNetAmount())
            .currency(payment.getCurrency())
            .status(CardStatus.ACTIVE)
            .expiresAt(Instant.now().plus(CARD_EXPIRY_DAYS, ChronoUnit.DAYS))
            .build();

        giftCard = giftCardRepository.save(giftCard);

        log.info("Created gift card: code={}, merchant={}, amount={}",
            cardCode, payment.getMerchant().getSymmetryMerchantId(), payment.getNetAmount());

        return giftCard;
    }

    @Transactional(readOnly = true)
    public GiftCardBalanceResponse getGiftCardBalance(String cardCode) {
        GiftCard giftCard = giftCardRepository.findByCardCode(cardCode)
            .orElseThrow(() -> new ResourceNotFoundException("Gift card not found with code: " + cardCode));

        return toBalanceResponse(giftCard);
    }

    @Transactional
    public RedemptionResponse redeemGiftCard(String cardCode, RedeemGiftCardRequest request) {
        if (redemptionRepository.existsByIdempotencyKey(request.getIdempotencyKey())) {
            GiftCardRedemption existingRedemption = redemptionRepository
                .findByIdempotencyKey(request.getIdempotencyKey())
                .orElseThrow();
            return toRedemptionResponse(existingRedemption);
        }

        GiftCard giftCard = giftCardRepository.findByCardCode(cardCode)
            .orElseThrow(() -> new ResourceNotFoundException("Gift card not found with code: " + cardCode));

        validateRedemption(giftCard, request);

        BigDecimal newBalance = giftCard.getBalance().subtract(request.getAmountToRedeem());

        giftCard.setBalance(newBalance);
        if (newBalance.compareTo(BigDecimal.ZERO) == 0) {
            giftCard.setStatus(CardStatus.FULLY_USED);
        } else if (newBalance.compareTo(giftCard.getInitialAmount()) < 0) {
            giftCard.setStatus(CardStatus.PARTIALLY_USED);
        }

        giftCard = giftCardRepository.save(giftCard);

        GiftCardRedemption redemption = GiftCardRedemption.builder()
            .giftCard(giftCard)
            .merchantId(request.getMerchantId())
            .idempotencyKey(request.getIdempotencyKey())
            .amountRedeemed(request.getAmountToRedeem())
            .remainingBalance(newBalance)
            .status(RedemptionStatus.COMPLETED)
            .redeemedAt(Instant.now())
            .build();

        redemption = redemptionRepository.save(redemption);

        log.info("Redeemed gift card: code={}, merchant={}, amount={}, remainingBalance={}",
            cardCode, request.getMerchantId(), request.getAmountToRedeem(), newBalance);

        createPayout(redemption);

        return toRedemptionResponse(redemption);
    }

    private void createPayout(GiftCardRedemption redemption) {
        UUID defaultOperatorId = operatorRepository.findByCode("WAVE")
            .orElseThrow(() -> new ResourceNotFoundException("Default operator not found"))
            .getId();

        BigDecimal feeAmount = operatorFeeService.calculateFee(
            defaultOperatorId,
            OperationType.PAYOUT,
            redemption.getAmountRedeemed()
        );

        BigDecimal netAmount = redemption.getAmountRedeemed().subtract(feeAmount);

        PayoutTransaction payout = PayoutTransaction.builder()
            .redemption(redemption)
            .merchantId(redemption.getMerchantId())
            .operator(operatorRepository.findById(defaultOperatorId).orElseThrow())
            .recipientNumber("221XXXXXXXXX")
            .amount(redemption.getAmountRedeemed())
            .feeAmount(feeAmount)
            .netAmount(netAmount)
            .currency(redemption.getGiftCard().getCurrency())
            .status(PayoutStatus.PENDING)
            .idempotencyKey(UUID.randomUUID().toString())
            .build();

        payoutRepository.save(payout);

        log.info("Created payout for redemption: redemptionId={}, merchant={}, amount={}",
            redemption.getId(), redemption.getMerchantId(), redemption.getAmountRedeemed());
    }

    private void validateRedemption(GiftCard giftCard, RedeemGiftCardRequest request) {
        if (giftCard.getStatus() == CardStatus.EXPIRED) {
            throw new BusinessException("Gift card has expired");
        }

        if (giftCard.getStatus() == CardStatus.BLOCKED) {
            throw new BusinessException("Gift card is blocked");
        }

        if (giftCard.getStatus() == CardStatus.FULLY_USED) {
            throw new BusinessException("Gift card has been fully used");
        }

        if (request.getAmountToRedeem().compareTo(giftCard.getBalance()) > 0) {
            throw new BusinessException("Insufficient balance. Available: " + giftCard.getBalance());
        }

        if (giftCard.getExpiresAt() != null && giftCard.getExpiresAt().isBefore(Instant.now())) {
            giftCard.setStatus(CardStatus.EXPIRED);
            giftCardRepository.save(giftCard);
            throw new BusinessException("Gift card has expired");
        }
    }

    private String generateUniqueCardCode() {
        SecureRandom random = new SecureRandom();
        String cardCode;
        do {
            StringBuilder sb = new StringBuilder(CARD_CODE_LENGTH);
            for (int i = 0; i < CARD_CODE_LENGTH; i++) {
                sb.append(CARD_CODE_CHARS.charAt(random.nextInt(CARD_CODE_CHARS.length())));
            }
            cardCode = sb.toString();
        } while (giftCardRepository.existsByCardCode(cardCode));

        return cardCode;
    }

    private GiftCardBalanceResponse toBalanceResponse(GiftCard giftCard) {
        return GiftCardBalanceResponse.builder()
            .id(giftCard.getId())
            .cardCode(giftCard.getCardCode())
            .merchantId(giftCard.getMerchantId())
            .initialAmount(giftCard.getInitialAmount())
            .balance(giftCard.getBalance())
            .currency(giftCard.getCurrency())
            .status(giftCard.getStatus())
            .expiresAt(giftCard.getExpiresAt())
            .createdAt(giftCard.getCreatedAt())
            .build();
    }

    private RedemptionResponse toRedemptionResponse(GiftCardRedemption redemption) {
        return RedemptionResponse.builder()
            .id(redemption.getId())
            .giftCardId(redemption.getGiftCard().getId())
            .merchantId(redemption.getMerchantId())
            .idempotencyKey(redemption.getIdempotencyKey())
            .amountRedeemed(redemption.getAmountRedeemed())
            .remainingBalance(redemption.getRemainingBalance())
            .status(redemption.getStatus())
            .redeemedAt(redemption.getRedeemedAt())
            .createdAt(redemption.getCreatedAt())
            .build();
    }
}
