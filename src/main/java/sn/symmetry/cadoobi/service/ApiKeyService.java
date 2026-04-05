package sn.symmetry.cadoobi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.symmetry.cadoobi.domain.entity.ApiKey;
import sn.symmetry.cadoobi.domain.entity.User;
import sn.symmetry.cadoobi.dto.ApiKeyResponse;
import sn.symmetry.cadoobi.dto.CreateApiKeyRequest;
import sn.symmetry.cadoobi.dto.UpdateApiKeyRequest;
import sn.symmetry.cadoobi.exception.ResourceNotFoundException;
import sn.symmetry.cadoobi.repository.ApiKeyRepository;
import sn.symmetry.cadoobi.repository.UserRepository;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApiKeyService {

    private final ApiKeyRepository apiKeyRepository;
    private final UserRepository userRepository;
    private final EncryptionService encryptionService;
    private static final String API_KEY_PREFIX = "pk_";
    private static final int API_KEY_LENGTH = 32; // bytes, will be 43 chars in base64

    @Transactional
    public ApiKeyResponse createApiKey(UUID userId, CreateApiKeyRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        String generatedKey = generateApiKey();
        String encryptedKey = encryptionService.encrypt(generatedKey);

        String allowedReferrersStr = request.getAllowedReferrers() != null && !request.getAllowedReferrers().isEmpty()
            ? String.join(",", request.getAllowedReferrers())
            : null;

        ApiKey apiKey = ApiKey.builder()
            .user(user)
            .apiKey(encryptedKey)
            .name(request.getName())
            .description(request.getDescription())
            .allowedReferrers(allowedReferrersStr)
            .expiresAt(request.getExpiresAt())
            .isActive(true)
            .build();

        apiKey = apiKeyRepository.save(apiKey);
        log.info("Created API key for user: userId={}, apiKeyId={}", userId, apiKey.getId());

        // Return response with full API key (only shown once)
        return toResponse(apiKey, true, generatedKey);
    }

    @Transactional(readOnly = true)
    public List<ApiKeyResponse> getAllApiKeys(UUID userId) {
        List<ApiKey> apiKeys = apiKeyRepository.findByUserId(userId);
        return apiKeys.stream()
            .map(key -> toResponse(key, false, null))
            .toList();
    }

    @Transactional(readOnly = true)
    public ApiKeyResponse getApiKeyById(UUID userId, UUID id) {
        ApiKey apiKey = findByIdAndUserId(id, userId);
        return toResponse(apiKey, false, null);
    }

    @Transactional
    public ApiKeyResponse updateApiKey(UUID userId, UUID id, UpdateApiKeyRequest request) {
        ApiKey apiKey = findByIdAndUserId(id, userId);

        if (request.getName() != null) {
            apiKey.setName(request.getName());
        }
        if (request.getDescription() != null) {
            apiKey.setDescription(request.getDescription());
        }
        if (request.getAllowedReferrers() != null) {
            String allowedReferrersStr = request.getAllowedReferrers().isEmpty()
                ? null
                : String.join(",", request.getAllowedReferrers());
            apiKey.setAllowedReferrers(allowedReferrersStr);
        }
        if (request.getIsActive() != null) {
            apiKey.setIsActive(request.getIsActive());
        }
        if (request.getExpiresAt() != null) {
            apiKey.setExpiresAt(request.getExpiresAt());
        }

        apiKey = apiKeyRepository.save(apiKey);
        log.info("Updated API key: userId={}, apiKeyId={}", userId, id);
        return toResponse(apiKey, false, null);
    }

    @Transactional
    public void deleteApiKey(UUID userId, UUID id) {
        ApiKey apiKey = findByIdAndUserId(id, userId);
        apiKeyRepository.delete(apiKey);
        log.info("Deleted API key: userId={}, apiKeyId={}", userId, id);
    }

    @Transactional
    public void updateLastUsed(String apiKey) {
        // Find by trying to match against all API keys
        apiKeyRepository.findAll().stream()
            .filter(key -> encryptionService.matches(apiKey, key.getApiKey()))
            .findFirst()
            .ifPresent(key -> {
                key.setLastUsedAt(Instant.now());
                apiKeyRepository.save(key);
            });
    }

    /**
     * Validates an API key and returns the associated user ID if valid.
     * Checks if key exists, is active, hasn't expired, and matches referrer restrictions.
     */
    @Transactional(readOnly = true)
    public UUID validateApiKey(String apiKey, String referrer) {
        // Find matching API key
        return apiKeyRepository.findAll().stream()
            .filter(key -> {
                try {
                    return encryptionService.matches(apiKey, key.getApiKey());
                } catch (Exception e) {
                    return false;
                }
            })
            .filter(ApiKey::getIsActive)
            .filter(key -> key.getExpiresAt() == null || key.getExpiresAt().isAfter(Instant.now()))
            .filter(key -> isReferrerAllowed(key, referrer))
            .findFirst()
            .map(key -> key.getUser().getId())
            .orElse(null);
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    private ApiKey findByIdAndUserId(UUID id, UUID userId) {
        ApiKey apiKey = apiKeyRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("API key not found with id: " + id));

        if (!apiKey.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("API key not found with id: " + id);
        }

        return apiKey;
    }

    private String generateApiKey() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[API_KEY_LENGTH];
        random.nextBytes(bytes);
        String encoded = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        return API_KEY_PREFIX + encoded;
    }

    private String maskApiKey(String plaintextKey) {
        if (plaintextKey == null || plaintextKey.length() < 10) {
            return "****";
        }
        String prefix = plaintextKey.substring(0, Math.min(7, plaintextKey.length()));
        String suffix = plaintextKey.substring(Math.max(plaintextKey.length() - 4, 7));
        return prefix + "****...****" + suffix;
    }

    private boolean isReferrerAllowed(ApiKey key, String referrer) {
        // If no referrer restrictions, allow all
        if (key.getAllowedReferrers() == null || key.getAllowedReferrers().isEmpty()) {
            return true;
        }

        // If no referrer provided but restrictions exist, deny
        if (referrer == null || referrer.isEmpty()) {
            return false;
        }

        // Check if referrer matches any allowed referrer
        List<String> allowedReferrers = List.of(key.getAllowedReferrers().split(","));
        return allowedReferrers.stream()
            .anyMatch(allowed -> referrer.trim().startsWith(allowed.trim()));
    }

    private ApiKeyResponse toResponse(ApiKey apiKey, boolean includeFullKey, String plaintextKey) {
        List<String> allowedReferrersList = apiKey.getAllowedReferrers() != null && !apiKey.getAllowedReferrers().isEmpty()
            ? List.of(apiKey.getAllowedReferrers().split(","))
            : List.of();

        // For masking, decrypt the key if we don't have the plaintext
        String keyForMasking = plaintextKey;
        if (keyForMasking == null) {
            try {
                keyForMasking = encryptionService.decrypt(apiKey.getApiKey());
            } catch (Exception e) {
                log.error("Error decrypting API key for masking", e);
                keyForMasking = "pk_****";
            }
        }

        return ApiKeyResponse.builder()
            .id(apiKey.getId())
            .apiKey(includeFullKey ? plaintextKey : null)
            .maskedApiKey(maskApiKey(keyForMasking))
            .name(apiKey.getName())
            .description(apiKey.getDescription())
            .allowedReferrers(allowedReferrersList)
            .isActive(apiKey.getIsActive())
            .expiresAt(apiKey.getExpiresAt())
            .lastUsedAt(apiKey.getLastUsedAt())
            .createdAt(apiKey.getCreatedAt())
            .updatedAt(apiKey.getUpdatedAt())
            .build();
    }
}
