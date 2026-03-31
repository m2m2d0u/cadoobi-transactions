package sn.symmetry.cadoobi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.symmetry.cadoobi.domain.entity.DefaultMerchantFee;
import sn.symmetry.cadoobi.domain.entity.Merchant;
import sn.symmetry.cadoobi.domain.entity.MerchantFee;
import sn.symmetry.cadoobi.domain.enums.FeeType;
import sn.symmetry.cadoobi.dto.CreateDefaultMerchantFeeRequest;
import sn.symmetry.cadoobi.dto.CreateMerchantFeeRequest;
import sn.symmetry.cadoobi.dto.DefaultMerchantFeeResponse;
import sn.symmetry.cadoobi.dto.MerchantFeeResponse;
import sn.symmetry.cadoobi.exception.BusinessException;
import sn.symmetry.cadoobi.exception.ResourceNotFoundException;
import sn.symmetry.cadoobi.repository.DefaultMerchantFeeRepository;
import sn.symmetry.cadoobi.repository.MerchantFeeRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MerchantFeeService {

    private final MerchantFeeRepository merchantFeeRepository;
    private final DefaultMerchantFeeRepository defaultMerchantFeeRepository;
    private final MerchantService merchantService;

    // ── Merchant fee endpoints ────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<MerchantFeeResponse> getMerchantFees(UUID merchantId) {
        merchantService.findById(merchantId);
        return merchantFeeRepository.findByMerchantId(merchantId).stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional
    public MerchantFeeResponse createMerchantFee(UUID merchantId, CreateMerchantFeeRequest request) {
        Merchant merchant = merchantService.findById(merchantId);
        validateFeeRequest(request.getFeeType(), request.getFeePercentage(), request.getFeeFixed(),
            request.getEffectiveFrom(), request.getEffectiveTo(), request.getMinAmount(), request.getMaxAmount());

        MerchantFee fee = MerchantFee.builder()
            .merchant(merchant)
            .feeType(request.getFeeType())
            .feePercentage(request.getFeePercentage())
            .feeFixed(request.getFeeFixed())
            .minAmount(request.getMinAmount())
            .maxAmount(request.getMaxAmount())
            .currency(request.getCurrency())
            .isActive(request.getIsActive())
            .effectiveFrom(request.getEffectiveFrom())
            .effectiveTo(request.getEffectiveTo())
            .build();

        fee = merchantFeeRepository.save(fee);
        log.info("Created fee for merchant {} ({}): {}", merchant.getName(), merchant.getCode(), request.getFeeType());
        return toResponse(fee);
    }

    @Transactional
    public MerchantFeeResponse updateMerchantFee(UUID merchantId, UUID feeId, CreateMerchantFeeRequest request) {
        merchantService.findById(merchantId);
        MerchantFee fee = merchantFeeRepository.findById(feeId)
            .orElseThrow(() -> new ResourceNotFoundException("Merchant fee not found with id: " + feeId));

        if (!fee.getMerchant().getId().equals(merchantId)) {
            throw new ResourceNotFoundException("Merchant fee not found with id: " + feeId);
        }

        validateFeeRequest(request.getFeeType(), request.getFeePercentage(), request.getFeeFixed(),
            request.getEffectiveFrom(), request.getEffectiveTo(), request.getMinAmount(), request.getMaxAmount());

        fee.setFeeType(request.getFeeType());
        fee.setFeePercentage(request.getFeePercentage());
        fee.setFeeFixed(request.getFeeFixed());
        fee.setMinAmount(request.getMinAmount());
        fee.setMaxAmount(request.getMaxAmount());
        fee.setCurrency(request.getCurrency());
        fee.setIsActive(request.getIsActive());
        fee.setEffectiveFrom(request.getEffectiveFrom());
        fee.setEffectiveTo(request.getEffectiveTo());

        fee = merchantFeeRepository.save(fee);
        log.info("Updated fee {} for merchant {}", feeId, merchantId);
        return toResponse(fee);
    }

    @Transactional
    public void deleteMerchantFee(UUID merchantId, UUID feeId) {
        merchantService.findById(merchantId);
        MerchantFee fee = merchantFeeRepository.findById(feeId)
            .orElseThrow(() -> new ResourceNotFoundException("Merchant fee not found with id: " + feeId));

        if (!fee.getMerchant().getId().equals(merchantId)) {
            throw new ResourceNotFoundException("Merchant fee not found with id: " + feeId);
        }

        merchantFeeRepository.delete(fee);
        log.info("Deleted fee {} for merchant {}", feeId, merchantId);
    }

    @Transactional(readOnly = true)
    public BigDecimal calculateFee(UUID merchantId, BigDecimal amount) {
        MerchantFee fee = merchantFeeRepository.findApplicableFee(
            merchantId, amount, LocalDate.now()
        ).orElse(null);

        if (fee == null) {
            log.debug("No fee configuration found for merchant {}, amount {}", merchantId, amount);
            return BigDecimal.ZERO;
        }

        return computeFee(amount, fee.getFeeType(), fee.getFeePercentage(), fee.getFeeFixed())
            .setScale(2, RoundingMode.HALF_UP);
    }

    // ── Default fee endpoints ─────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<DefaultMerchantFeeResponse> getAllDefaultFees() {
        return defaultMerchantFeeRepository.findAll().stream()
            .map(this::toDefaultResponse)
            .toList();
    }

    @Transactional
    public DefaultMerchantFeeResponse createDefaultFee(CreateDefaultMerchantFeeRequest request) {
        validateFeeRequest(request.getFeeType(), request.getFeePercentage(), request.getFeeFixed(),
            LocalDate.now(), request.getEffectiveTo(), request.getMinAmount(), request.getMaxAmount());

        DefaultMerchantFee fee = DefaultMerchantFee.builder()
            .description(request.getDescription())
            .feeType(request.getFeeType())
            .feePercentage(request.getFeePercentage())
            .feeFixed(request.getFeeFixed())
            .minAmount(request.getMinAmount())
            .maxAmount(request.getMaxAmount())
            .currency(request.getCurrency())
            .isActive(request.getIsActive())
            .effectiveTo(request.getEffectiveTo())
            .build();

        fee = defaultMerchantFeeRepository.save(fee);
        log.info("Created default merchant fee: {}", request.getFeeType());
        return toDefaultResponse(fee);
    }

    @Transactional
    public DefaultMerchantFeeResponse updateDefaultFee(UUID id, CreateDefaultMerchantFeeRequest request) {
        DefaultMerchantFee fee = defaultMerchantFeeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Default merchant fee not found with id: " + id));

        validateFeeRequest(request.getFeeType(), request.getFeePercentage(), request.getFeeFixed(),
            LocalDate.now(), request.getEffectiveTo(), request.getMinAmount(), request.getMaxAmount());

        fee.setDescription(request.getDescription());
        fee.setFeeType(request.getFeeType());
        fee.setFeePercentage(request.getFeePercentage());
        fee.setFeeFixed(request.getFeeFixed());
        fee.setMinAmount(request.getMinAmount());
        fee.setMaxAmount(request.getMaxAmount());
        fee.setCurrency(request.getCurrency());
        fee.setIsActive(request.getIsActive());
        fee.setEffectiveTo(request.getEffectiveTo());

        fee = defaultMerchantFeeRepository.save(fee);
        log.info("Updated default merchant fee: {}", id);
        return toDefaultResponse(fee);
    }

    @Transactional
    public void deleteDefaultFee(UUID id) {
        DefaultMerchantFee fee = defaultMerchantFeeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Default merchant fee not found with id: " + id));
        defaultMerchantFeeRepository.delete(fee);
        log.info("Deleted default merchant fee: {}", id);
    }

    /**
     * Copies all active default fees to the given merchant.
     * Called by MerchantService when creating a new merchant.
     */
    @Transactional
    public void applyDefaultFeesToMerchant(Merchant merchant) {
        List<DefaultMerchantFee> defaults = defaultMerchantFeeRepository.findByIsActiveTrue();
        if (defaults.isEmpty()) {
            log.debug("No active default fees to apply to merchant {}", merchant.getCode());
            return;
        }

        List<MerchantFee> fees = defaults.stream()
            .map(d -> MerchantFee.builder()
                .merchant(merchant)
                .feeType(d.getFeeType())
                .feePercentage(d.getFeePercentage())
                .feeFixed(d.getFeeFixed())
                .minAmount(d.getMinAmount())
                .maxAmount(d.getMaxAmount())
                .currency(d.getCurrency())
                .isActive(d.getIsActive())
                .effectiveFrom(LocalDate.now())
                .effectiveTo(d.getEffectiveTo())
                .build())
            .toList();

        merchantFeeRepository.saveAll(fees);
        log.info("Applied {} default fee(s) to merchant {} ({})",
            fees.size(), merchant.getName(), merchant.getCode());
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private void validateFeeRequest(FeeType feeType, BigDecimal feePercentage, BigDecimal feeFixed,
                                    LocalDate effectiveFrom, LocalDate effectiveTo,
                                    BigDecimal minAmount, BigDecimal maxAmount) {
        if (feeType == FeeType.PERCENTAGE || feeType == FeeType.MIXED) {
            if (feePercentage == null) {
                throw new BusinessException("Fee percentage is required for PERCENTAGE or MIXED fee type");
            }
        }
        if (feeType == FeeType.FIXED || feeType == FeeType.MIXED) {
            if (feeFixed == null) {
                throw new BusinessException("Fixed fee is required for FIXED or MIXED fee type");
            }
        }
        if (effectiveTo != null && effectiveFrom != null && effectiveTo.isBefore(effectiveFrom)) {
            throw new BusinessException("Effective to date must be after effective from date");
        }
        if (maxAmount != null && minAmount != null && maxAmount.compareTo(minAmount) < 0) {
            throw new BusinessException("Maximum amount must be greater than or equal to minimum amount");
        }
    }

    private BigDecimal computeFee(BigDecimal amount, FeeType feeType,
                                  BigDecimal feePercentage, BigDecimal feeFixed) {
        BigDecimal result = BigDecimal.ZERO;
        if ((feeType == FeeType.PERCENTAGE || feeType == FeeType.MIXED) && feePercentage != null) {
            result = result.add(amount.multiply(feePercentage));
        }
        if ((feeType == FeeType.FIXED || feeType == FeeType.MIXED) && feeFixed != null) {
            result = result.add(feeFixed);
        }
        return result;
    }

    private MerchantFeeResponse toResponse(MerchantFee fee) {
        return MerchantFeeResponse.builder()
            .id(fee.getId())
            .merchantId(fee.getMerchant().getId())
            .feeType(fee.getFeeType())
            .feePercentage(fee.getFeePercentage())
            .feeFixed(fee.getFeeFixed())
            .minAmount(fee.getMinAmount())
            .maxAmount(fee.getMaxAmount())
            .currency(fee.getCurrency())
            .isActive(fee.getIsActive())
            .effectiveFrom(fee.getEffectiveFrom())
            .effectiveTo(fee.getEffectiveTo())
            .build();
    }

    private DefaultMerchantFeeResponse toDefaultResponse(DefaultMerchantFee fee) {
        return DefaultMerchantFeeResponse.builder()
            .id(fee.getId())
            .description(fee.getDescription())
            .feeType(fee.getFeeType())
            .feePercentage(fee.getFeePercentage())
            .feeFixed(fee.getFeeFixed())
            .minAmount(fee.getMinAmount())
            .maxAmount(fee.getMaxAmount())
            .currency(fee.getCurrency())
            .isActive(fee.getIsActive())
            .effectiveTo(fee.getEffectiveTo())
            .build();
    }
}
