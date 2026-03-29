package sn.symmetry.cadoobi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request payload for redeeming a gift card")
public class RedeemGiftCardRequest {

    @Schema(description = "Unique merchant identifier", example = "MCH-001", required = true)
    @NotBlank(message = "Merchant ID is required")
    @Size(max = 36, message = "Merchant ID must not exceed 36 characters")
    private String merchantId;

    @Schema(description = "Amount to redeem from the gift card", example = "5000", required = true)
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amountToRedeem;

    @Schema(description = "Unique key to prevent duplicate redemption requests", example = "REDEEM-20240326-ABC123", required = true)
    @NotBlank(message = "Idempotency key is required")
    @Size(max = 255, message = "Idempotency key must not exceed 255 characters")
    private String idempotencyKey;
}
