package de.unipassau.allocationsystem.utils;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

/**
 * Utility class for creating standardized HTTP response entities.
 * Provides methods for common response patterns (success, error, etc.).
 */
public class ResponseHandler {
    
    /**
     * Creates a successful response (200 OK) with message and data.
     * 
     * @param message the success message
     * @param data the response data
     * @return response entity with 200 status
     */
    public static ResponseEntity<?> success(String message, Object data) {
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", message,
            "data", data
        ));
    }

    /**
     * Creates a created response (201 CREATED) with message and data.
     * 
     * @param message the success message
     * @param data the created resource data
     * @return response entity with 201 status
     */
    public static ResponseEntity<?> created(String message, Object data) {
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
            "success", true,
            "message", message,
            "data", data
        ));
    }

    /**
     * Creates an updated response (200 OK) with message and data.
     * 
     * @param message the success message
     * @param data the updated resource data
     * @return response entity with 200 status
     */
    public static ResponseEntity<?> updated(String message, Object data) {
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", message,
            "data", data
        ));
    }

    /**
     * Creates a no content response (204 NO CONTENT).
     * 
     * @return response entity with 204 status and empty body
     */
    public static ResponseEntity<?> noContent() {
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * Creates a bad request response (400 BAD REQUEST) with message and data.
     * 
     * @param message the error message
     * @param data additional error data
     * @return response entity with 400 status
     */
    public static ResponseEntity<?> badRequest(String message, Object data) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
            "success", false,
            "message", message,
            "data", data
        ));
    }

    /**
     * Creates an unauthorized response (401 UNAUTHORIZED) with message.
     * 
     * @param message the error message
     * @return response entity with 401 status
     */
    public static ResponseEntity<?> unauthorized(String message) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
            "success", false,
            "message", message,
            "data", Map.of()
        ));
    }

    /**
     * Creates a forbidden response (403 FORBIDDEN) with message.
     * 
     * @param message the error message
     * @return response entity with 403 status
     */
    public static ResponseEntity<?> forbidden(String message) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
            "success", false,
            "message", message,
            "data", Map.of()
        ));
    }

    /**
     * Creates a not found response (404 NOT FOUND) with message.
     * 
     * @param message the error message
     * @return response entity with 404 status
     */
    public static ResponseEntity<?> notFound(String message) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
            "success", false,
            "message", message,
            "data", Map.of()
        ));
    }

    /**
     * Creates a conflict response (409 CONFLICT) with message and data.
     * 
     * @param message the error message
     * @param data additional conflict data
     * @return response entity with 409 status
     */
    public static ResponseEntity<?> conflict(String message, Object data) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
            "success", false,
            "message", message,
            "data", data
        ));
    }

    /**
     * Creates a server error response (500 INTERNAL SERVER ERROR) with message and data.
     * 
     * @param message the error message
     * @param data additional error data
     * @return response entity with 500 status
     */
    public static ResponseEntity<?> serverError(String message, Object data) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
            "success", false,
            "message", message,
            "data", data
        ));
    }

    /**
     * Creates a paginated success response (200 OK) with message and pagination data.
     * 
     * @param message the success message
     * @param data the pagination data map
     * @return response entity with 200 status
     */
    public static ResponseEntity<?> paginated(String message, Map<String, Object> data) {
        data.put("success", true);
        data.put("message", message);
        return ResponseEntity.ok(data);
    }
}
