package de.unipassau.allocationsystem.controller.docs;

import de.unipassau.allocationsystem.dto.academicyear.AcademicYearResponseDto;
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
 * Meta-annotation for documenting UPDATE operations in REST controllers.
 * Combines Swagger documentation for a PUT endpoint that updates an existing entity.
 * Automatically generates OpenAPI documentation with 200, 400, 404, and 500 response codes.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Operation(summary = "Update entity", description = "Updates an existing entity with the provided details")
@ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Entity updated successfully",
        content = @Content(schema = @Schema(implementation = AcademicYearResponseDto.class))),
    @ApiResponse(responseCode = "400", description = "Invalid input or duplicate value"),
    @ApiResponse(responseCode = "404", description = "Entity not found"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
})
public @interface UpdateDocs {}
