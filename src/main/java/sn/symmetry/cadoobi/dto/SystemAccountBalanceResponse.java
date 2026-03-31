package sn.symmetry.cadoobi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@Schema(description = "System account balance (platform earnings)")
public class SystemAccountBalanceResponse {

    @Schema(description = "System account ID")
    private UUID accountId;

    @Schema(description = "Currency code (ISO 4217)", example = "XOF")
    private String currency;

    @Schema(description = "Total platform earnings balance", example = "250000.00")
    private BigDecimal balance;

    @Schema(description = "Last updated timestamp")
    private Instant updatedAt;
}
