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
 * Meta-annotation for documenting GET ALL operations for Permission endpoints.
 * Combines Swagger documentation for retrieving all permissions without pagination.
 * Automatically generates OpenAPI documentation with 200 and 500 response codes.
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Operation(summary = "Get all permissions", description = "Retrieves all permissions without pagination")
@ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Permissions retrieved successfully",
        content = @Content(schema = @Schema(implementation = PermissionResponseDto.class))),
    @ApiResponse(responseCode = "500", description = "Internal server error")
})
public @interface GetAllPermissionsDocs { }
