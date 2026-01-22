package de.unipassau.allocationsystem.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
/**
 * Global exception handler for REST API endpoints.
 * Provides centralized exception handling and standardized error responses.
 */
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private Map<String, Object> buildBody(HttpStatus status, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", false);
        body.put("message", message);
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("timestamp", Instant.now().toString());
        return body;
    }

    /**
     * Helper method to create a response entity with standard error structure.
     * Reduces code duplication across exception handlers.
     * 
     * @param status HTTP status code
     * @param message Error message
     * @param logLevel Log level (ERROR, WARN, INFO)
     * @return ResponseEntity with error details
     */
    private ResponseEntity<Map<String, Object>> createErrorResponse(HttpStatus status, String message, String logLevel) {
        if ("ERROR".equals(logLevel)) {
            LOGGER.error(message);
        } else if ("WARN".equals(logLevel)) {
            LOGGER.warn(message);
        }
        Map<String, Object> body = buildBody(status, message);
        return ResponseEntity.status(status).body(body);
    }

    /**
     * Handles validation errors from bean validation annotations.
     * 
     * @param ex the validation exception
     * @return response entity with validation error details
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(err ->
                errors.put(err.getField(), err.getDefaultMessage())
        );

        Map<String, Object> body = buildBody(HttpStatus.BAD_REQUEST, "Validation failed");
        body.put("errors", errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /**
     * Handles resource not found exceptions.
     * 
     * @param ex the resource not found exception
     * @return response entity with 404 status
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFound(ResourceNotFoundException ex) {
        return createErrorResponse(HttpStatus.NOT_FOUND, "Resource not found: " + ex.getMessage(), "WARN");
    }

    /**
     * Handles duplicate resource exceptions.
     * 
     * @param ex the duplicate resource exception
     * @return response entity with 409 conflict status
     */
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateResource(DuplicateResourceException ex) {
        return createErrorResponse(HttpStatus.CONFLICT, "Duplicate resource: " + ex.getMessage(), "WARN");
    }

    /**
     * Handles data integrity constraint violations from the database.
     * 
     * @param ex the data integrity violation exception
     * @return response entity with 409 conflict status
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        return createErrorResponse(HttpStatus.CONFLICT, "Data integrity constraint violated", "ERROR");
    }

    /**
     * Handles authentication failures including bad credentials and user not found.
     * 
     * @param ex the authentication exception
     * @return response entity with 401 unauthorized status
     */
    @ExceptionHandler({BadCredentialsException.class, UsernameNotFoundException.class})
    public ResponseEntity<Map<String, Object>> handleAuthenticationException(Exception ex) {
        LOGGER.warn("Authentication failed: {}", ex.getMessage());
        Map<String, Object> body = buildBody(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    /**
     * Handles locked account exceptions.
     * 
     * @param ex the locked exception
     * @return response entity with 401 unauthorized status
     */
    @ExceptionHandler(LockedException.class)
    public ResponseEntity<Map<String, Object>> handleAccountLocked(LockedException ex) {
        LOGGER.warn("Account locked: {}", ex.getMessage());
        Map<String, Object> body = buildBody(HttpStatus.UNAUTHORIZED, ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    /**
     * Handles disabled account exceptions.
     * 
     * @param ex the disabled exception
     * @return response entity with 401 unauthorized status
     */
    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<Map<String, Object>> handleAccountDisabled(DisabledException ex) {
        LOGGER.warn("Account disabled: {}", ex.getMessage());
        Map<String, Object> body = buildBody(HttpStatus.UNAUTHORIZED, "Account is disabled");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    /**
     * Handles illegal argument exceptions.
     * 
     * @param ex the illegal argument exception
     * @return response entity with 400 bad request status
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        return createErrorResponse(HttpStatus.BAD_REQUEST, "Invalid argument: " + ex.getMessage(), "WARN");
    }

    /**
     * Handles illegal state exceptions.
     * 
     * @param ex the illegal state exception
     * @return response entity with 401 unauthorized status
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(IllegalStateException ex) {
        return createErrorResponse(HttpStatus.UNAUTHORIZED, "Invalid state: " + ex.getMessage(), "WARN");
    }

    /**
     * Handles authorization denied exceptions.
     * 
     * @param ex the authorization denied exception
     * @return response entity with 403 forbidden status
     */
    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAuthorizationDenied(AuthorizationDeniedException ex) {
        return createErrorResponse(HttpStatus.FORBIDDEN, "Access denied", "WARN");
    }

    /**
     * Handles all unhandled exceptions as a fallback.
     * 
     * @param ex the generic exception
     * @return response entity with 500 internal server error status
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        LOGGER.error("Unhandled exception", ex);
        Map<String, Object> body = buildBody(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    /**
     * Handles no such element exceptions.
     * 
     * @param ex the no such element exception
     * @return response entity with 404 not found status
     */
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, Object>> handleNoSuchElement(NoSuchElementException ex) {
        LOGGER.warn("No such element: {}", ex.getMessage());
        Map<String, Object> body = buildBody(HttpStatus.NOT_FOUND, ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    /**
     * Handles malformed or unreadable HTTP request body exceptions.
     * 
     * @param ex the message not readable exception
     * @return response entity with 400 bad request status
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        LOGGER.warn("Invalid request body: {}", ex.getMessage());
        String message = "Invalid request body. " + ex.getMostSpecificCause().getMessage();
        Map<String, Object> body = buildBody(HttpStatus.BAD_REQUEST, message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /**
     * Handles I/O exceptions during file operations.
     * 
     * @param ex the I/O exception
     * @return response entity with 500 internal server error status
     */
    @ExceptionHandler(IOException.class)
    public ResponseEntity<Map<String, Object>> handleIOException(IOException ex) {
        LOGGER.error("I/O error occurred: {}", ex.getMessage(), ex);
        Map<String, Object> body = buildBody(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to process file operation: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
