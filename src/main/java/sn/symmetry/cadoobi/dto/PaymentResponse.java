package sn.symmetry.cadoobi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import sn.symmetry.cadoobi.domain.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Payment transaction details")
public class PaymentResponse {

    @Schema(description = "Unique payment identifier", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @Schema(description = "Payment reference", example = "PAY-20240326-123456")
    private String reference;

    @Schema(description = "Merchant identifier", example = "MCH-001")
    private String merchantId;

    @Schema(description = "Merchant code", example = "SHOP123")
    private String merchantCode;

    @Schema(description = "Operator code", example = "ORANGE_MONEY")
    private String operatorCode;

    @Schema(description = "Payment amount", example = "10000")
    private BigDecimal amount;

    @Schema(description = "Fee amount charged", example = "250")
    private BigDecimal feeAmount;

    @Schema(description = "Net amount after fees", example = "9750")
    private BigDecimal netAmount;

    @Schema(description = "Currency code", example = "XOF")
    private String currency;

    @Schema(description = "Current payment status", example = "INITIATED")
    private PaymentStatus status;

    @Schema(description = "Operator transaction identifier", example = "OM-TXN-987654")
    private String operatorTransactionId;

    @Schema(description = "Payment URL for completing the transaction", example = "https://pay.orange.sn/checkout/abc123")
    private String paymentUrl;

    @Schema(description = "Payment expiration timestamp", example = "2024-03-27T10:30:00Z")
    private Instant expiresAt;

    @Schema(description = "Creation timestamp", example = "2024-03-26T10:30:00Z")
    private Instant createdAt;
}
