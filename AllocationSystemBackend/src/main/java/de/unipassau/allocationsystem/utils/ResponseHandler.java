package de.unipassau.allocationsystem.utils;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.Map;

public class ResponseHandler {
    
    public static ResponseEntity<?> success(String message, Object data) {
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", message,
            "data", data
        ));
    }

    public static ResponseEntity<?> created(String message, Object data) {
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
            "success", true,
            "message", message,
            "data", data
        ));
    }

    public static ResponseEntity<?> updated(String message, Object data) {
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
            "success", true,
            "message", message,
            "data", data
        ));
    }

    public static ResponseEntity<?> noContent() {
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    public static ResponseEntity<?> badRequest(String message, Object data) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
            "success", false,
            "message", message,
            "data", data
        ));
    }

    public static ResponseEntity<?> unauthorized(String message) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
            "success", false,
            "message", message,
            "data", Map.of()
        ));
    }

    public static ResponseEntity<?> forbidden(String message) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
            "success", false,
            "message", message,
            "data", Map.of()
        ));
    }

    public static ResponseEntity<?> notFound(String message) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
            "success", false,
            "message", message,
            "data", Map.of()
        ));
    }

    public static ResponseEntity<?> conflict(String message, Object data) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
            "success", false,
            "message", message,
            "data", data
        ));
    }

    public static ResponseEntity<?> serverError(String message, Object data) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
            "success", false,
            "message", message,
            "data", data
        ));
    }

    public static ResponseEntity<?> paginated(String message, Map<String, Object> data) {
        data.put("success", true);
        data.put("message", message);
        return ResponseEntity.ok(data);
    }
}
