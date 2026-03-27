package sn.symmetry.cadoobi.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateMerchantRequest {

    @NotBlank(message = "Code is required")
    @Size(max = 10, message = "Code must not exceed 10 characters")
    private String code;

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @Size(max = 500, message = "Logo URL must not exceed 500 characters")
    private String logoUrl;

    @NotBlank(message = "Phone is required")
    @Size(max = 15, message = "Phone must not exceed 15 characters")
    private String phone;

    @Size(max = 100, message = "Business type must not exceed 100 characters")
    private String businessType;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @Size(max = 255, message = "Address must not exceed 255 characters")
    private String address;

    @NotBlank(message = "Country is required")
    @Size(min = 2, max = 2, message = "Country must be a 2-character ISO code")
    private String country;

    @Size(max = 100, message = "RCCM must not exceed 100 characters")
    private String rccm;

    @Size(max = 50, message = "NINEA must not exceed 50 characters")
    private String ninea;

    // Owner
    @Size(max = 150, message = "Owner full name must not exceed 150 characters")
    private String ownerFullName;

    @Email(message = "Owner email must be valid")
    @Size(max = 100, message = "Owner email must not exceed 100 characters")
    private String ownerEmail;

    @Size(max = 15, message = "Owner phone must not exceed 15 characters")
    private String ownerPhone;

    @Size(max = 50, message = "Owner CNI must not exceed 50 characters")
    private String ownerCni;

    // Compensation account
    @Valid
    private CompensationAccountDto compensationAccount;
}
