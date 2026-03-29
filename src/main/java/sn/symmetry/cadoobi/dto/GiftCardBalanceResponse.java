package sn.symmetry.cadoobi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import sn.symmetry.cadoobi.domain.enums.CardStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Gift card balance and status information")
public class GiftCardBalanceResponse {

    @Schema(description = "Unique gift card identifier", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @Schema(description = "Gift card code", example = "ABC123DEF456")
    private String cardCode;

    @Schema(description = "Merchant identifier", example = "MCH-001")
    private String merchantId;

    @Schema(description = "Initial gift card amount", example = "50000")
    private BigDecimal initialAmount;

    @Schema(description = "Current available balance", example = "35000")
    private BigDecimal balance;

    @Schema(description = "Currency code", example = "XOF")
    private String currency;

    @Schema(description = "Current card status", example = "ACTIVE")
    private CardStatus status;

    @Schema(description = "Card expiration timestamp", example = "2025-03-26T10:30:00Z")
    private Instant expiresAt;

    @Schema(description = "Card creation timestamp", example = "2024-03-26T10:30:00Z")
    private Instant createdAt;
}
