package sn.symmetry.cadoobi.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.annotation.Lazy;
import sn.symmetry.cadoobi.domain.entity.CompensationAccount;
import sn.symmetry.cadoobi.domain.entity.Merchant;
import sn.symmetry.cadoobi.domain.entity.MerchantAccount;
import sn.symmetry.cadoobi.domain.entity.Operator;
import sn.symmetry.cadoobi.domain.entity.User;
import sn.symmetry.cadoobi.domain.enums.CompensationAccountType;
import sn.symmetry.cadoobi.domain.enums.MerchantStatus;
import sn.symmetry.cadoobi.dto.CompensationAccountDto;
import sn.symmetry.cadoobi.dto.CreateMerchantRequest;
import sn.symmetry.cadoobi.dto.MerchantResponse;
import sn.symmetry.cadoobi.dto.UpdateMerchantRequest;
import sn.symmetry.cadoobi.exception.DuplicateResourceException;
import sn.symmetry.cadoobi.exception.ResourceNotFoundException;
import sn.symmetry.cadoobi.repository.MerchantAccountRepository;
import sn.symmetry.cadoobi.repository.MerchantRepository;
import sn.symmetry.cadoobi.repository.UserRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class MerchantService {

    private final MerchantRepository merchantRepository;
    private final UserRepository userRepository;
    private final OperatorService operatorService;
    private final MerchantFeeService merchantFeeService;
    private final MerchantAccountRepository merchantAccountRepository;

    public MerchantService(MerchantRepository merchantRepository,
                           UserRepository userRepository,
                           OperatorService operatorService,
                           @Lazy MerchantFeeService merchantFeeService,
                           MerchantAccountRepository merchantAccountRepository) {
        this.merchantRepository = merchantRepository;
        this.userRepository = userRepository;
        this.operatorService = operatorService;
        this.merchantFeeService = merchantFeeService;
        this.merchantAccountRepository = merchantAccountRepository;
    }

    @Transactional(readOnly = true)
    public List<MerchantResponse> getAllMerchants() {
        return merchantRepository.findAll().stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<MerchantResponse> getMerchantsByStatus(MerchantStatus status) {
        return merchantRepository.findByStatus(status).stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public Page<MerchantResponse> getAllMerchants(Pageable pageable) {
        return merchantRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<MerchantResponse> getMerchantsByStatus(MerchantStatus status, Pageable pageable) {
        return merchantRepository.findByStatus(status, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public MerchantResponse getMerchantById(UUID id) {
        return toResponse(findById(id));
    }

    @Transactional(readOnly = true)
    public MerchantResponse getMerchantByCode(String code) {
        return toResponse(findByCode(code));
    }

    @Transactional
    public MerchantResponse createMerchant(CreateMerchantRequest request) {
        if (merchantRepository.existsByCode(request.getCode())) {
            throw new DuplicateResourceException("Merchant with code " + request.getCode() + " already exists");
        }
        if (merchantRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Merchant with email " + request.getEmail() + " already exists");
        }

        User user = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.getUserId()));

        Merchant merchant = Merchant.builder()
            .code(request.getCode().toUpperCase())
            .name(request.getName())
            .logoUrl(request.getLogoUrl())
            .phone(request.getPhone())
            .businessType(request.getBusinessType())
            .email(request.getEmail())
            .address(request.getAddress())
            .country(request.getCountry().toUpperCase())
            .rccm(request.getRccm())
            .ninea(request.getNinea())
            .ownerFullName(request.getOwnerFullName())
            .ownerEmail(request.getOwnerEmail())
            .ownerPhone(request.getOwnerPhone())
            .ownerCni(request.getOwnerCni())
            .compensationAccount(toCompensationAccount(request.getCompensationAccount()))
            .user(user)
            .status(MerchantStatus.PENDING)
            .build();

        merchant = merchantRepository.save(merchant);
        merchantFeeService.applyDefaultFeesToMerchant(merchant);
        merchantAccountRepository.save(MerchantAccount.builder()
            .merchant(merchant)
            .currency("XOF")
            .balance(BigDecimal.ZERO)
            .lockedBalance(BigDecimal.ZERO)
            .build());
        log.info("Created merchant: code={}, name={}", merchant.getCode(), merchant.getName());
        return toResponse(merchant);
    }

    @Transactional
    public MerchantResponse updateMerchant(UUID id, UpdateMerchantRequest request) {
        Merchant merchant = findById(id);

        merchant.setName(request.getName());
        merchant.setLogoUrl(request.getLogoUrl());
        merchant.setPhone(request.getPhone());
        merchant.setBusinessType(request.getBusinessType());
        merchant.setEmail(request.getEmail());
        merchant.setAddress(request.getAddress());
        merchant.setCountry(request.getCountry().toUpperCase());
        merchant.setRccm(request.getRccm());
        merchant.setNinea(request.getNinea());
        merchant.setOwnerFullName(request.getOwnerFullName());
        merchant.setOwnerEmail(request.getOwnerEmail());
        merchant.setOwnerPhone(request.getOwnerPhone());
        merchant.setOwnerCni(request.getOwnerCni());
        merchant.setCompensationAccount(toCompensationAccount(request.getCompensationAccount()));
        if (request.getStatus() != null) {
            merchant.setStatus(request.getStatus());
        }
        if (request.getUserId() != null) {
            User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.getUserId()));
            merchant.setUser(user);
        }

        merchant = merchantRepository.save(merchant);
        log.info("Updated merchant: code={}, status={}", merchant.getCode(), merchant.getStatus());
        return toResponse(merchant);
    }

    @Transactional
    public void deleteMerchant(UUID id) {
        Merchant merchant = findById(id);
        merchantRepository.delete(merchant);
        log.info("Deleted merchant: code={}", merchant.getCode());
    }

    @Transactional
    public MerchantResponse updateMerchantStatus(UUID id, MerchantStatus status) {
        Merchant merchant = findById(id);
        merchant.setStatus(status);
        merchant = merchantRepository.save(merchant);
        log.info("Updated merchant status: code={}, status={}", merchant.getCode(), merchant.getStatus());
        return toResponse(merchant);
    }

    @Transactional(readOnly = true)
    public Page<MerchantResponse> getMerchantsByUserId(UUID userId, Pageable pageable) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found: " + userId);
        }
        return merchantRepository.findByUserId(userId, pageable).map(this::toResponse);
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    public Merchant findById(UUID id) {
        return merchantRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Merchant not found with id: " + id));
    }

    public Merchant findByCode(String code) {
        return merchantRepository.findByCode(code)
            .orElseThrow(() -> new ResourceNotFoundException("Merchant not found with code: " + code));
    }

    private CompensationAccount toCompensationAccount(CompensationAccountDto dto) {
        if (dto == null) return null;

        CompensationAccount.CompensationAccountBuilder builder = CompensationAccount.builder()
            .type(dto.getType())
            .bankName(dto.getBankName())
            .accountNumber(dto.getAccountNumber())
            .accountHolder(dto.getAccountHolder())
            .iban(dto.getIban())
            .swift(dto.getSwift())
            .operatorPhone(dto.getOperatorPhone())
            .operatorHolderName(dto.getOperatorHolderName());

        if (dto.getType() == CompensationAccountType.OPERATOR && dto.getOperatorCode() != null) {
            Operator operator = operatorService.getOperatorByCode(dto.getOperatorCode());
            builder.operator(operator);
        }

        return builder.build();
    }

    private MerchantResponse toResponse(Merchant merchant) {
        CompensationAccountDto compDto = null;
        if (merchant.getCompensationAccount() != null) {
            CompensationAccount comp = merchant.getCompensationAccount();
            CompensationAccountDto.CompensationAccountDtoBuilder dtoBuilder = CompensationAccountDto.builder()
                .type(comp.getType())
                .bankName(comp.getBankName())
                .accountNumber(comp.getAccountNumber())
                .accountHolder(comp.getAccountHolder())
                .iban(comp.getIban())
                .swift(comp.getSwift())
                .operatorPhone(comp.getOperatorPhone())
                .operatorHolderName(comp.getOperatorHolderName());

            if (comp.getOperator() != null) {
                dtoBuilder
                    .operatorCode(comp.getOperator().getCode())
                    .operatorName(comp.getOperator().getName());
            }

            compDto = dtoBuilder.build();
        }

        return MerchantResponse.builder()
            .id(merchant.getId())
            .code(merchant.getCode())
            .name(merchant.getName())
            .logoUrl(merchant.getLogoUrl())
            .phone(merchant.getPhone())
            .businessType(merchant.getBusinessType())
            .email(merchant.getEmail())
            .address(merchant.getAddress())
            .country(merchant.getCountry())
            .rccm(merchant.getRccm())
            .ninea(merchant.getNinea())
            .ownerFullName(merchant.getOwnerFullName())
            .ownerEmail(merchant.getOwnerEmail())
            .ownerPhone(merchant.getOwnerPhone())
            .ownerCni(merchant.getOwnerCni())
            .compensationAccount(compDto)
            .symmetryMerchantId(merchant.getSymmetryMerchantId())
            .agencyCode(merchant.getAgencyCode())
            .status(merchant.getStatus())
            .userId(merchant.getUser() != null ? merchant.getUser().getId() : null)
            .userFullName(merchant.getUser() != null ? merchant.getUser().getFullName() : null)
            .userEmail(merchant.getUser() != null ? merchant.getUser().getEmail() : null)
            .createdAt(merchant.getCreatedAt())
            .updatedAt(merchant.getUpdatedAt())
            .build();
    }
}
