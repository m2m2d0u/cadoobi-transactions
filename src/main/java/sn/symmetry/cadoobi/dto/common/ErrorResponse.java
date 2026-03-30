package sn.symmetry.cadoobi.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Error response structure")
public class ErrorResponse {

    @Schema(description = "Error timestamp", example = "2024-03-26T10:30:00Z")
    private Instant timestamp;

    @Schema(description = "HTTP status code", example = "404")
    private Integer status;

    @Schema(description = "Error type", example = "Not Found")
    private String error;

    @Schema(description = "Error message", example = "Resource not found")
    private String message;

    @Schema(description = "Request path", example = "/api/resource/123")
    private String path;

    public static ErrorResponse of(Integer status, String error, String message, String path) {
        return ErrorResponse.builder()
            .timestamp(Instant.now())
            .status(status)
            .error(error)
            .message(message)
            .path(path)
            .build();
    }
}
