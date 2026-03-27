package sn.symmetry.cadoobi.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
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
public class ApiResponse<T> {

    /**
     * Indicates if the request was successful
     */
    private Boolean success;

    /**
     * Human-readable message about the response
     */
    private String message;

    /**
     * The actual response data (null for errors)
     */
    private T data;

    /**
     * Error details (null for successful responses)
     */
    private ErrorDetails error;

    /**
     * Timestamp of the response
     */
    @Builder.Default
    private Instant timestamp = Instant.now();

    /**
     * Request path (optional)
     */
    private String path;

    /**
     * Error details nested object
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ErrorDetails {
        /**
         * HTTP status code
         */
        private Integer code;

        /**
         * Error type/category
         */
        private String type;

        /**
         * Detailed error message
         */
        private String message;

        /**
         * Additional error details (validation errors, etc.)
         */
        private Object details;
    }

    // ==================== Success Response Builders ====================

    /**
     * Creates a successful response with data
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message("Request completed successfully")
                .data(data)
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Creates a successful response with data and custom message
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Creates a successful response with only a message (no data)
     */
    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Creates a successful response for creation operations (201)
     */
    public static <T> ApiResponse<T> created(T data, String message) {
        return ApiResponse.<T>builder()
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
    public static <T> ApiResponse<T> error(String message, Integer code, String type) {
        return ApiResponse.<T>builder()
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
    public static <T> ApiResponse<T> error(String message, Integer code, String type, String path) {
        return ApiResponse.<T>builder()
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
    public static <T> ApiResponse<T> error(String message, Integer code, String type, String path, Object details) {
        return ApiResponse.<T>builder()
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
    public static <T> ApiResponse<T> notFound(String message, String path) {
        return error(message, 404, "NOT_FOUND", path);
    }

    /**
     * Creates a 400 Bad Request error response
     */
    public static <T> ApiResponse<T> badRequest(String message, String path) {
        return error(message, 400, "BAD_REQUEST", path);
    }

    /**
     * Creates a 400 Bad Request error response with validation details
     */
    public static <T> ApiResponse<T> validationError(String message, String path, Object validationDetails) {
        return error(message, 400, "VALIDATION_ERROR", path, validationDetails);
    }

    /**
     * Creates a 409 Conflict error response
     */
    public static <T> ApiResponse<T> conflict(String message, String path) {
        return error(message, 409, "CONFLICT", path);
    }

    /**
     * Creates a 500 Internal Server Error response
     */
    public static <T> ApiResponse<T> internalError(String message, String path) {
        return error(message, 500, "INTERNAL_SERVER_ERROR", path);
    }

    /**
     * Creates a 401 Unauthorized error response
     */
    public static <T> ApiResponse<T> unauthorized(String message, String path) {
        return error(message, 401, "UNAUTHORIZED", path);
    }

    /**
     * Creates a 403 Forbidden error response
     */
    public static <T> ApiResponse<T> forbidden(String message, String path) {
        return error(message, 403, "FORBIDDEN", path);
    }
}
