package sn.symmetry.cadoobi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import sn.symmetry.cadoobi.domain.enums.MerchantStatus;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Merchant details and configuration")
public class MerchantResponse {

    @Schema(description = "Unique merchant identifier", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @Schema(description = "Merchant code", example = "MERCH001")
    private String code;

    @Schema(description = "Merchant name", example = "Boutique Fatou")
    private String name;

    @Schema(description = "Merchant logo URL", example = "https://cdn.example.com/logo.png")
    private String logoUrl;

    @Schema(description = "Merchant phone number", example = "+221771234567")
    private String phone;

    @Schema(description = "Business type", example = "RETAIL")
    private String businessType;

    @Schema(description = "Merchant email", example = "contact@boutique.sn")
    private String email;

    @Schema(description = "Business address", example = "Avenue Blaise Diagne, Dakar")
    private String address;

    @Schema(description = "Country code (ISO 3166-1 alpha-2)", example = "SN")
    private String country;

    @Schema(description = "Commerce registry number (RCCM)", example = "SN-DKR-2020-A-12345")
    private String rccm;

    @Schema(description = "Tax identification number (NINEA)", example = "123456789")
    private String ninea;

    // Owner
    @Schema(description = "Business owner full name", example = "Amadou Diallo")
    private String ownerFullName;

    @Schema(description = "Business owner email", example = "amadou@email.com")
    private String ownerEmail;

    @Schema(description = "Business owner phone", example = "+221779876543")
    private String ownerPhone;

    @Schema(description = "Business owner national ID (CNI)", example = "1234567890123")
    private String ownerCni;

    // Compensation account
    @Schema(description = "Compensation account details for receiving funds")
    private CompensationAccountDto compensationAccount;

    // Symmetry fields
    @Schema(description = "Symmetry platform merchant ID", example = "SYM-MERCH-001")
    private String symmetryMerchantId;

    @Schema(description = "Agency code", example = "AG-DAKAR-01")
    private String agencyCode;

    @Schema(description = "Merchant status", example = "ACTIVE")
    private MerchantStatus status;

    @Schema(description = "Creation timestamp", example = "2024-03-26T10:30:00Z")
    private Instant createdAt;

    @Schema(description = "Last update timestamp", example = "2024-03-26T10:30:00Z")
    private Instant updatedAt;
}
