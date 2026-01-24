package de.unipassau.allocationsystem.controller.docs.permission;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Meta-annotation for documenting GET SORT FIELDS operations for Permission endpoints.
 * Combines Swagger documentation for retrieving available sortable permission fields.
 * Automatically generates OpenAPI documentation with 200 and 500 response codes.
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Operation(summary = "Get permission sort fields", description = "Retrieves available fields for sorting permissions")
@ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Sort fields retrieved successfully"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
})
public @interface GetPermissionSortFieldsDocs { }
