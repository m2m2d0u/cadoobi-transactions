package sn.symmetry.cadoobi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sn.symmetry.cadoobi.domain.enums.MerchantStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to update merchant status")
public class UpdateMerchantStatusRequest {

    @NotNull(message = "Status is required")
    @Schema(description = "New merchant status", required = true, example = "ACTIVE")
    private MerchantStatus status;
}
