package de.unipassau.allocationsystem.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Meta-annotation for documenting DELETE operations in REST controllers.
 * Combines Swagger documentation for a DELETE endpoint that removes an entity.
 * Automatically generates OpenAPI documentation with 204, 404, and 500 response codes.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Operation(summary = "Delete entity", description = "Deletes an entity by its ID")
@ApiResponses(value = {
    @ApiResponse(responseCode = "204", description = "Entity deleted successfully"),
    @ApiResponse(responseCode = "404", description = "Entity not found"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
})
public @interface DeleteDocs {}
