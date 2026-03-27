package sn.symmetry.cadoobi.dto;

import lombok.*;
import sn.symmetry.cadoobi.domain.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {

    private UUID id;
    private String reference;
    private String merchantCode;
    private String merchantName;
    private String operatorCode;
    private BigDecimal amount;
    private BigDecimal feeAmount;
    private BigDecimal netAmount;
    private String currency;
    private PaymentStatus status;
    private String operatorTransactionId;
    private String paymentUrl;
    private Instant expiresAt;
    private Instant createdAt;
}
