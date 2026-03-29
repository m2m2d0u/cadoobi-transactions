package sn.symmetry.cadoobi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;
import sn.symmetry.cadoobi.domain.enums.MerchantStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request payload for updating an existing merchant")
public class UpdateMerchantRequest {

    @Schema(description = "Merchant business name", example = "Boutique Fatou", required = true)
    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @Schema(description = "URL to merchant logo image", example = "https://cdn.example.com/logo.png")
    @Size(max = 500, message = "Logo URL must not exceed 500 characters")
    private String logoUrl;

    @Schema(description = "Merchant contact phone number", example = "+221771234567", required = true)
    @NotBlank(message = "Phone is required")
    @Size(max = 15, message = "Phone must not exceed 15 characters")
    private String phone;

    @Schema(description = "Type of business", example = "RETAIL")
    @Size(max = 100, message = "Business type must not exceed 100 characters")
    private String businessType;

    @Schema(description = "Merchant contact email", example = "contact@boutique.sn", required = true)
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @Schema(description = "Business physical address", example = "Avenue Blaise Diagne, Dakar")
    @Size(max = 255, message = "Address must not exceed 255 characters")
    private String address;

    @Schema(description = "Country code (ISO 3166-1 alpha-2)", example = "SN", required = true)
    @NotBlank(message = "Country is required")
    @Size(min = 2, max = 2, message = "Country must be a 2-character ISO code")
    private String country;

    @Schema(description = "Commerce registry number (RCCM)", example = "SN-DKR-2020-A-12345")
    @Size(max = 100, message = "RCCM must not exceed 100 characters")
    private String rccm;

    @Schema(description = "Tax identification number (NINEA)", example = "123456789")
    @Size(max = 50, message = "NINEA must not exceed 50 characters")
    private String ninea;

    // Owner
    @Schema(description = "Business owner full name", example = "Amadou Diallo")
    @Size(max = 150, message = "Owner full name must not exceed 150 characters")
    private String ownerFullName;

    @Schema(description = "Business owner email", example = "amadou@email.com")
    @Email(message = "Owner email must be valid")
    @Size(max = 100, message = "Owner email must not exceed 100 characters")
    private String ownerEmail;

    @Schema(description = "Business owner phone number", example = "+221779876543")
    @Size(max = 15, message = "Owner phone must not exceed 15 characters")
    private String ownerPhone;

    @Schema(description = "Business owner national ID (CNI)", example = "1234567890123")
    @Size(max = 50, message = "Owner CNI must not exceed 50 characters")
    private String ownerCni;

    // Compensation account
    @Schema(description = "Compensation account for receiving merchant funds")
    @Valid
    private CompensationAccountDto compensationAccount;

    // Status change
    @Schema(description = "Merchant account status", example = "ACTIVE")
    private MerchantStatus status;
}
