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

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Operation(summary = "Get entity by ID", description = "Retrieves a specific entity by its ID")
@ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Entity found",
        content = @Content(schema = @Schema(implementation = AcademicYearResponseDto.class))),
    @ApiResponse(responseCode = "404", description = "Entity not found"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
})
public @interface GetByIdDocs {}
