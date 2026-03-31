package sn.symmetry.cadoobi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import sn.symmetry.cadoobi.domain.enums.LedgerDirection;
import sn.symmetry.cadoobi.domain.enums.LedgerEntryType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@Schema(description = "A single ledger entry (immutable financial event)")
public class LedgerEntryResponse {

    @Schema(description = "Ledger entry ID")
    private UUID id;

    @Schema(description = "Merchant account ID")
    private UUID merchantAccountId;

    @Schema(description = "Credit or Debit", example = "CREDIT")
    private LedgerDirection direction;

    @Schema(description = "Type of ledger event", example = "PAYIN_SETTLEMENT")
    private LedgerEntryType entryType;

    @Schema(description = "Amount of this entry", example = "9800.00")
    private BigDecimal amount;

    @Schema(description = "Currency code", example = "XOF")
    private String currency;

    @Schema(description = "Human-readable description")
    private String description;

    @Schema(description = "Idempotency key for this entry")
    private String idempotencyKey;

    @Schema(description = "Associated payment transaction ID, if any")
    private UUID paymentTransactionId;

    @Schema(description = "Associated payout transaction ID, if any")
    private UUID payoutTransactionId;

    @Schema(description = "When this entry was created")
    private Instant createdAt;
}
