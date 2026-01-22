package de.unipassau.allocationsystem.controller;

import de.unipassau.allocationsystem.utils.ResponseHandler;
import org.springframework.http.ResponseEntity;

/**
 * Base class for REST API controllers providing common response handling methods.
 * Centralizes ResponseHandler calls to reduce code duplication across controllers.
 */
public abstract class ApiControllerSupport {

    /**
     * Returns a success response with the provided message and data.
     *
     * @param <T> The type of data being returned
     * @param message Success message to include in response
     * @param data The data payload to return
     * @return ResponseEntity with 200 OK status
     */
    protected <T> ResponseEntity<?> ok(String message, T data) {
        return ResponseHandler.success(message, data);
    }

    /**
     * Returns a created response with the provided message and data.
     *
     * @param <T> The type of data being returned
     * @param message Success message to include in response
     * @param data The newly created data payload
     * @return ResponseEntity with 201 Created status
     */
    protected <T> ResponseEntity<?> created(String message, T data) {
        return ResponseHandler.created(message, data);
    }

    /**
     * Returns an updated response with the provided message and data.
     *
     * @param <T> The type of data being returned
     * @param message Success message to include in response
     * @param data The updated data payload
     * @return ResponseEntity with 200 OK status
     */
    protected <T> ResponseEntity<?> updated(String message, T data) {
        return ResponseHandler.updated(message, data);
    }

    /**
     * Returns a no-content response (typically for delete operations).
     *
     * @return ResponseEntity with 204 No Content status
     */
    protected ResponseEntity<?> noContent() {
        return ResponseHandler.noContent();
    }
}
