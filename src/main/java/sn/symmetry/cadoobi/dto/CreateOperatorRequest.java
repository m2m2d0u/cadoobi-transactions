package sn.symmetry.cadoobi.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOperatorRequest {

    @NotBlank(message = "Code is required")
    @Size(max = 30, message = "Code must not exceed 30 characters")
    private String code;

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @NotBlank(message = "Country is required")
    @Size(min = 2, max = 2, message = "Country must be exactly 2 characters")
    private String country;

    private Boolean supportsPayin = true;

    private Boolean supportsPayout = true;

    @Size(max = 500, message = "API base URL must not exceed 500 characters")
    private String apiBaseUrl;

    private Boolean isActive = true;
}
