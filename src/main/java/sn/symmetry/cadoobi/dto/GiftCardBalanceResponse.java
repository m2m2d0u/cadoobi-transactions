package sn.symmetry.cadoobi.dto;

import lombok.*;
import sn.symmetry.cadoobi.domain.enums.CardStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GiftCardBalanceResponse {

    private UUID id;
    private String cardCode;
    private String merchantId;
    private BigDecimal initialAmount;
    private BigDecimal balance;
    private String currency;
    private CardStatus status;
    private Instant expiresAt;
    private Instant createdAt;
}
