package sn.symmetry.cadoobi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request payload for initiating a payment transaction")
public class InitiatePaymentRequest {

    @Schema(description = "Unique merchant identifier", example = "MCH-001", required = true)
    @NotBlank(message = "Merchant ID is required")
    @Size(max = 36, message = "Merchant ID must not exceed 36 characters")
    private String merchantId;

    @Schema(description = "Merchant code identifier", example = "SHOP123", required = true)
    @NotBlank(message = "Merchant code is required")
    @Size(max = 10, message = "Merchant code must not exceed 10 characters")
    private String merchantCode;

    @Schema(description = "Payment operator code", example = "ORANGE_MONEY", required = true)
    @NotBlank(message = "Operator code is required")
    private String operatorCode;

    @Schema(description = "Payment amount", example = "10000", required = true)
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @Schema(description = "Currency code (ISO 4217)", example = "XOF", defaultValue = "XOF")
    @Size(max = 3, message = "Currency must be 3 characters")
    private String currency = "XOF";

    @Schema(description = "Payer phone number", example = "+221771234567", required = true)
    @NotBlank(message = "Payer phone is required")
    @Size(max = 20, message = "Payer phone must not exceed 20 characters")
    private String payerPhone;

    @Schema(description = "Payer full name", example = "Amadou Diallo")
    @Size(max = 150, message = "Payer full name must not exceed 150 characters")
    private String payerFullName;

    @Schema(description = "Recipient phone number", example = "+221779876543", required = true)
    @NotBlank(message = "Recipient phone is required")
    @Size(max = 20, message = "Recipient phone must not exceed 20 characters")
    private String recipientPhone;

    @Schema(description = "Recipient name", example = "Fatou Sall")
    @Size(max = 150, message = "Recipient name must not exceed 150 characters")
    private String recipientName;

    @Schema(description = "URL to receive payment status callbacks", example = "https://merchant.com/webhooks/payment", required = true)
    @NotBlank(message = "Callback URL is required")
    @Size(max = 500, message = "Callback URL must not exceed 500 characters")
    private String callbackUrl;
}
