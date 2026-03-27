package sn.symmetry.cadoobi.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InitiatePaymentRequest {

    @NotBlank(message = "Merchant code is required")
    @Size(max = 10, message = "Merchant code must not exceed 10 characters")
    private String merchantCode;

    @NotBlank(message = "Operator code is required")
    private String operatorCode;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @Size(max = 3, message = "Currency must be 3 characters")
    private String currency = "XOF";

    @NotBlank(message = "Payer phone is required")
    @Size(max = 20, message = "Payer phone must not exceed 20 characters")
    private String payerPhone;

    @Size(max = 150, message = "Payer full name must not exceed 150 characters")
    private String payerFullName;

    @NotBlank(message = "Recipient phone is required")
    @Size(max = 20, message = "Recipient phone must not exceed 20 characters")
    private String recipientPhone;

    @Size(max = 150, message = "Recipient name must not exceed 150 characters")
    private String recipientName;

    @NotBlank(message = "Callback URL is required")
    @Size(max = 500, message = "Callback URL must not exceed 500 characters")
    private String callbackUrl;
}
