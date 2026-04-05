package sn.symmetry.cadoobi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload for operator payment callbacks")
public class OperatorCallbackRequest {

    @NotBlank(message = "Operator reference is required")
    @Schema(description = "Unique transaction reference from the operator", required = true, example = "OP-123456789")
    private String operatorReference;

    @NotBlank(message = "Payment reference is required")
    @Schema(description = "Our internal payment reference", required = true, example = "CADOOBI01-20240405-ABC123")
    private String paymentReference;

    @NotBlank(message = "Status is required")
    @Schema(description = "Payment status from operator (SUCCESS, FAILED, PENDING, etc.)", required = true, example = "SUCCESS")
    private String status;

    @Schema(description = "Additional message or description from operator", example = "Payment completed successfully")
    private String message;

    @Schema(description = "Operator transaction ID", example = "TXN-987654321")
    private String operatorTransactionId;

    @Schema(description = "Raw JSON payload from operator for audit purposes")
    private String rawPayload;
}
