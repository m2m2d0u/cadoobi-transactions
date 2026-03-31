package sn.symmetry.cadoobi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import sn.symmetry.cadoobi.domain.enums.LedgerDirection;
import sn.symmetry.cadoobi.domain.enums.SystemEntryType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@Schema(description = "A single system account entry (immutable platform earnings event)")
public class SystemAccountEntryResponse {

    @Schema(description = "System account entry ID")
    private UUID id;

    @Schema(description = "System account ID")
    private UUID systemAccountId;

    @Schema(description = "Credit or Debit", example = "CREDIT")
    private LedgerDirection direction;

    @Schema(description = "Type of system entry event", example = "MERCHANT_FEE_EARNED")
    private SystemEntryType entryType;

    @Schema(description = "Amount of this entry", example = "1000.00")
    private BigDecimal amount;

    @Schema(description = "Currency code", example = "XOF")
    private String currency;

    @Schema(description = "Human-readable description")
    private String description;

    @Schema(description = "Idempotency key for this entry")
    private String idempotencyKey;

    @Schema(description = "Associated payout transaction ID, if any")
    private UUID payoutTransactionId;

    @Schema(description = "When this entry was created")
    private Instant createdAt;
}
