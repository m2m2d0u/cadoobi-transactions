package sn.symmetry.cadoobi.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.Instant;

/**
 * Generic API response wrapper for consistent response format across all endpoints.
 *
 * @param <T> The type of data being returned
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Generic API response wrapper providing consistent structure across all endpoints")
public class ControllerApiResponse<T> {

    /**
     * Indicates if the request was successful
     */
    @Schema(description = "Indicates whether the request was successful", example = "true")
    private Boolean success;

    /**
     * Human-readable message about the response
     */
    @Schema(description = "Human-readable message about the response", example = "Request completed successfully")
    private String message;

    /**
     * The actual response data (null for errors)
     */
    @Schema(description = "The actual response data (null for error responses)")
    private T data;

    /**
     * Error details (null for successful responses)
     */
    @Schema(description = "Error details (null for successful responses)")
    private ErrorDetails error;

    /**
     * Timestamp of the response
     */
    @Schema(description = "Timestamp when the response was generated", example = "2024-03-26T10:30:00Z")
    @Builder.Default
    private Instant timestamp = Instant.now();

    /**
     * Request path (optional)
     */
    @Schema(description = "Request path that generated this response", example = "/payments/PAY-123")
    private String path;

    /**
     * Error details nested object
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "Detailed error information for failed requests")
    public static class ErrorDetails {
        /**
         * HTTP status code
         */
        @Schema(description = "HTTP status code", example = "404")
        private Integer code;

        /**
         * Error type/category
         */
        @Schema(description = "Error type or category", example = "NOT_FOUND")
        private String type;

        /**
         * Detailed error message
         */
        @Schema(description = "Detailed error message", example = "Payment not found")
        private String message;

        /**
         * Additional error details (validation errors, etc.)
         */
        @Schema(description = "Additional error details such as validation errors")
        private Object details;
    }

    // ==================== Success Response Builders ====================

    /**
     * Creates a successful response with data
     */
    public static <T> ControllerApiResponse<T> success(T data) {
        return ControllerApiResponse.<T>builder()
                .success(true)
                .message("Request completed successfully")
                .data(data)
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Creates a successful response with data and custom message
     */
    public static <T> ControllerApiResponse<T> success(T data, String message) {
        return ControllerApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Creates a successful response with only a message (no data)
     */
    public static <T> ControllerApiResponse<T> success(String message) {
        return ControllerApiResponse.<T>builder()
                .success(true)
                .message(message)
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Creates a successful response for creation operations (201)
     */
    public static <T> ControllerApiResponse<T> created(T data, String message) {
        return ControllerApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(Instant.now())
                .build();
    }

    // ==================== Error Response Builders ====================

    /**
     * Creates an error response
     */
    public static <T> ControllerApiResponse<T> error(String message, Integer code, String type) {
        return ControllerApiResponse.<T>builder()
                .success(false)
                .message(message)
                .error(ErrorDetails.builder()
                        .code(code)
                        .type(type)
                        .message(message)
                        .build())
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Creates an error response with path
     */
    public static <T> ControllerApiResponse<T> error(String message, Integer code, String type, String path) {
        return ControllerApiResponse.<T>builder()
                .success(false)
                .message(message)
                .error(ErrorDetails.builder()
                        .code(code)
                        .type(type)
                        .message(message)
                        .build())
                .path(path)
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Creates an error response with additional details
     */
    public static <T> ControllerApiResponse<T> error(String message, Integer code, String type, String path, Object details) {
        return ControllerApiResponse.<T>builder()
                .success(false)
                .message(message)
                .error(ErrorDetails.builder()
                        .code(code)
                        .type(type)
                        .message(message)
                        .details(details)
                        .build())
                .path(path)
                .timestamp(Instant.now())
                .build();
    }

    // ==================== Common Error Responses ====================

    /**
     * Creates a 404 Not Found error response
     */
    public static <T> ControllerApiResponse<T> notFound(String message, String path) {
        return error(message, 404, "NOT_FOUND", path);
    }

    /**
     * Creates a 400 Bad Request error response
     */
    public static <T> ControllerApiResponse<T> badRequest(String message, String path) {
        return error(message, 400, "BAD_REQUEST", path);
    }

    /**
     * Creates a 400 Bad Request error response with validation details
     */
    public static <T> ControllerApiResponse<T> validationError(String message, String path, Object validationDetails) {
        return error(message, 400, "VALIDATION_ERROR", path, validationDetails);
    }

    /**
     * Creates a 409 Conflict error response
     */
    public static <T> ControllerApiResponse<T> conflict(String message, String path) {
        return error(message, 409, "CONFLICT", path);
    }

    /**
     * Creates a 500 Internal Server Error response
     */
    public static <T> ControllerApiResponse<T> internalError(String message, String path) {
        return error(message, 500, "INTERNAL_SERVER_ERROR", path);
    }

    /**
     * Creates a 401 Unauthorized error response
     */
    public static <T> ControllerApiResponse<T> unauthorized(String message, String path) {
        return error(message, 401, "UNAUTHORIZED", path);
    }

    /**
     * Creates a 403 Forbidden error response
     */
    public static <T> ControllerApiResponse<T> forbidden(String message, String path) {
        return error(message, 403, "FORBIDDEN", path);
    }
}
