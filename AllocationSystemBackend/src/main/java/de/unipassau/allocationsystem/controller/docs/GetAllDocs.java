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
 * Meta-annotation for documenting GET ALL operations in REST controllers.
 * Combines Swagger documentation for a GET endpoint that retrieves all entities without pagination.
 * Automatically generates OpenAPI documentation with 200 and 500 response codes.
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Operation(summary = "Get all entities", description = "Retrieves all entities without pagination")
@ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Entities retrieved successfully",
        content = @Content(schema = @Schema(implementation = AcademicYearResponseDto.class))),
    @ApiResponse(responseCode = "500", description = "Internal server error")
})
public @interface GetAllDocs { }
