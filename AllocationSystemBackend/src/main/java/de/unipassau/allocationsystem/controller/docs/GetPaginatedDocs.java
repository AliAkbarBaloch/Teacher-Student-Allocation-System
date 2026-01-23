package de.unipassau.allocationsystem.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Meta-annotation for documenting PAGINATED GET operations in REST controllers.
 * Combines Swagger documentation for a GET endpoint that retrieves entities with pagination, sorting, and search.
 * Automatically generates OpenAPI documentation with 200, 400, and 500 response codes.
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Operation(summary = "Get paginated entities", description = "Retrieves entities with pagination, sorting, and optional search")
@ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Entities retrieved successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid pagination parameters"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
})
public @interface GetPaginatedDocs { }
