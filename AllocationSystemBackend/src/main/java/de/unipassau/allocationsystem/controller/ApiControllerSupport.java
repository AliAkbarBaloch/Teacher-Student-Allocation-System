package de.unipassau.allocationsystem.controller;

import de.unipassau.allocationsystem.utils.ResponseHandler;
import org.springframework.http.ResponseEntity;

/**
 * Base class for REST API controllers providing common response handling methods.
 * Centralizes {@link ResponseHandler} calls to reduce code duplication across controllers.
 */
public abstract class ApiControllerSupport {

    /**
     * Returns a 200 (OK) response produced by {@link ResponseHandler#success(String, Object)}.
     *
     * @param message success message to include in the response
     * @param data payload to include in the response
     * @param <T> payload type
     * @return a {@link ResponseEntity} with HTTP 200 (OK)
     */
    protected <T> ResponseEntity<?> ok(String message, T data) {
        return ResponseHandler.success(message, data);
    }

    /**
     * Returns a 201 (Created) response produced by {@link ResponseHandler#created(String, Object)}.
     *
     * @param message success message to include in the response
     * @param data payload to include in the response
     * @param <T> payload type
     * @return a {@link ResponseEntity} with HTTP 201 (Created)
     */
    protected <T> ResponseEntity<?> created(String message, T data) {
        return ResponseHandler.created(message, data);
    }

    /**
     * Returns a 200 (OK) response produced by {@link ResponseHandler#updated(String, Object)}.
     *
     * @param message success message to include in the response
     * @param data payload to include in the response
     * @param <T> payload type
     * @return a {@link ResponseEntity} with HTTP 200 (OK)
     */
    protected <T> ResponseEntity<?> updated(String message, T data) {
        return ResponseHandler.updated(message, data);
    }

    /**
     * Returns a 204 (No Content) response produced by {@link ResponseHandler#noContent()}.
     *
     * @return a {@link ResponseEntity} with HTTP 204 (No Content)
     */
    protected ResponseEntity<?> noContent() {
        return ResponseHandler.noContent();
    }
}
