package sn.symmetry.cadoobi.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.symmetry.cadoobi.domain.enums.MerchantStatus;
import sn.symmetry.cadoobi.dto.ApiResponse;
import sn.symmetry.cadoobi.dto.CreateMerchantRequest;
import sn.symmetry.cadoobi.dto.MerchantResponse;
import sn.symmetry.cadoobi.dto.UpdateMerchantRequest;
import sn.symmetry.cadoobi.service.MerchantService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/merchants")
@RequiredArgsConstructor
@Slf4j
public class MerchantController {

    private final MerchantService merchantService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<MerchantResponse>>> getAllMerchants(
        @RequestParam(required = false) MerchantStatus status
    ) {
        List<MerchantResponse> merchants = status != null
            ? merchantService.getMerchantsByStatus(status)
            : merchantService.getAllMerchants();

        return ResponseEntity.ok(ApiResponse.success(
            merchants,
            merchants.size() + " merchant(s) found"
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MerchantResponse>> getMerchantById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(
            merchantService.getMerchantById(id),
            "Merchant retrieved successfully"
        ));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<MerchantResponse>> getMerchantByCode(@PathVariable String code) {
        return ResponseEntity.ok(ApiResponse.success(
            merchantService.getMerchantByCode(code),
            "Merchant retrieved successfully"
        ));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<MerchantResponse>> createMerchant(
        @Valid @RequestBody CreateMerchantRequest request
    ) {
        log.info("Creating merchant: code={}, name={}", request.getCode(), request.getName());
        MerchantResponse merchant = merchantService.createMerchant(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.created(merchant, "Merchant created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MerchantResponse>> updateMerchant(
        @PathVariable UUID id,
        @Valid @RequestBody UpdateMerchantRequest request
    ) {
        log.info("Updating merchant: id={}", id);
        MerchantResponse merchant = merchantService.updateMerchant(id, request);
        return ResponseEntity.ok(ApiResponse.success(merchant, "Merchant updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMerchant(@PathVariable UUID id) {
        log.info("Deleting merchant: id={}", id);
        merchantService.deleteMerchant(id);
        return ResponseEntity.ok(ApiResponse.success("Merchant deleted successfully"));
    }
}
