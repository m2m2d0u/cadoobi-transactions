package sn.symmetry.cadoobi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@Schema(description = "Merchant ledger account balance")
public class MerchantBalanceResponse {

    @Schema(description = "Merchant account ID")
    private UUID accountId;

    @Schema(description = "Merchant ID")
    private UUID merchantId;

    @Schema(description = "Currency code (ISO 4217)", example = "XOF")
    private String currency;

    @Schema(description = "Total balance (including locked funds)", example = "150000.00")
    private BigDecimal balance;

    @Schema(description = "Funds locked for pending payouts", example = "50000.00")
    private BigDecimal lockedBalance;

    @Schema(description = "Available balance (balance - lockedBalance)", example = "100000.00")
    private BigDecimal availableBalance;

    @Schema(description = "Last updated timestamp")
    private Instant updatedAt;
}
