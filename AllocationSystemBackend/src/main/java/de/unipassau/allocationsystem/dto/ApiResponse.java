package de.unipassau.allocationsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Generic API response wrapper.
 * Standardizes response format across all API endpoints with success flag, message, and data payload.
 *
 * @param <T> Type of the data payload
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;

    /**
     * Creates a successful API response with provided data.
     *
     * @param success Success status flag
     * @param message Response message
     * @param data Response data payload
     * @param <T> Type of the data
     * @return ApiResponse instance
     */
    public static <T> ApiResponse<T> success(boolean success, String message, T data) {
        return ApiResponse.<T>builder()
                .success(success)
                .message(message)
                .data(data)
                .build();
    }
}
