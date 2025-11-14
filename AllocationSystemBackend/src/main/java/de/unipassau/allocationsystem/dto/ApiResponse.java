package de.unipassau.allocationsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;

    public static <T> ApiResponse<T> success(boolean success, String message, T data) {
        return ApiResponse.<T>builder()
                .success(success)
                .message(message)
                .data(data)
                .build();
    }
}
