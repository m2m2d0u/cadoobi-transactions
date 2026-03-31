package sn.symmetry.cadoobi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Request payload for initiating a merchant payout")
public class CreatePayoutRequest {

    @Schema(description = "Symmetry merchant ID (external identifier)", required = true)
    @NotBlank(message = "Merchant ID is required")
    private String merchantId;

    @Schema(description = "Operator code to use for the payout", example = "WAVE", required = true)
    @NotBlank(message = "Operator code is required")
    private String operatorCode;

    @Schema(description = "Recipient mobile number", example = "221770000000", required = true)
    @NotBlank(message = "Recipient number is required")
    private String recipientNumber;

    @Schema(description = "Amount to pay out (declared amount)", example = "50000", required = true)
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be positive")
    private BigDecimal amount;

    @Schema(description = "Currency code (ISO 4217)", example = "XOF", defaultValue = "XOF")
    @Size(max = 3)
    private String currency = "XOF";

    @Schema(description = "Unique idempotency key to prevent duplicate payouts", required = true)
    @NotBlank(message = "Idempotency key is required")
    private String idempotencyKey;
}
