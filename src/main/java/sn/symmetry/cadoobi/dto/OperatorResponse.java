package sn.symmetry.cadoobi.dto;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OperatorResponse {

    private UUID id;
    private String code;
    private String name;
    private String country;
    private Boolean supportsPayin;
    private Boolean supportsPayout;
    private String apiBaseUrl;
    private Boolean isActive;
    private Instant createdAt;
}
