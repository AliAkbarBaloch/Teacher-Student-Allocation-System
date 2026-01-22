package de.unipassau.allocationsystem.controller.docs.permission;

import de.unipassau.allocationsystem.dto.permission.PermissionResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Meta-annotation for documenting GET BY ID operations for Permission endpoints.
 * Combines Swagger documentation for retrieving a specific permission by its ID.
 * Automatically generates OpenAPI documentation with 200, 404, and 500 response codes.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Operation(summary = "Get permission by ID", description = "Retrieves a specific permission by its ID")
@ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Permission found",
        content = @Content(schema = @Schema(implementation = PermissionResponseDto.class))),
    @ApiResponse(responseCode = "404", description = "Permission not found"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
})
public @interface GetPermissionByIdDocs {}
