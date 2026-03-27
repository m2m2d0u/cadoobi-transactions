package sn.symmetry.cadoobi.dto;

import lombok.*;
import sn.symmetry.cadoobi.domain.enums.MerchantStatus;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MerchantResponse {

    private UUID id;
    private String code;
    private String name;
    private String logoUrl;
    private String phone;
    private String businessType;
    private String email;
    private String address;
    private String country;
    private String rccm;
    private String ninea;

    // Owner
    private String ownerFullName;
    private String ownerEmail;
    private String ownerPhone;
    private String ownerCni;

    // Compensation account
    private CompensationAccountDto compensationAccount;

    // Symmetry fields
    private String symmetryMerchantId;
    private String agencyCode;
    private MerchantStatus status;

    private Instant createdAt;
    private Instant updatedAt;
}
