package sn.symmetry.cadoobi.dto;

import lombok.*;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {

    private Instant timestamp;
    private Integer status;
    private String error;
    private String message;
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
