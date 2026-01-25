package de.unipassau.allocationsystem.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
/**
 * DTO for standardized error responses across the API.
 * Contains error code, message, and optional details.
 */
public class ErrorResponse {
    private String code;
    private String message;
    private Object details;

    /**
     * Constructs an ErrorResponse with code and message only.
     * 
     * @param code the error code
     * @param message the error message
     */
    public ErrorResponse(String code, String message) {
        this.code = code;
        this.message = message;
        this.details = null;
    }
}
