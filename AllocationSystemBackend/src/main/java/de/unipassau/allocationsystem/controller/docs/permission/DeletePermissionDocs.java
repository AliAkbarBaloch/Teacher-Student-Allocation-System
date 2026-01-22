package de.unipassau.allocationsystem.controller.docs.permission;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Meta-annotation for documenting DELETE operations for Permission endpoints.
 * Combines Swagger documentation for deleting a permission by its ID.
 * Automatically generates OpenAPI documentation with 204, 404, and 500 response codes.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Operation(summary = "Delete permission", description = "Deletes a permission by its ID")
@ApiResponses(value = {
    @ApiResponse(responseCode = "204", description = "Permission deleted successfully"),
    @ApiResponse(responseCode = "404", description = "Permission not found"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
})
public @interface DeletePermissionDocs {}
