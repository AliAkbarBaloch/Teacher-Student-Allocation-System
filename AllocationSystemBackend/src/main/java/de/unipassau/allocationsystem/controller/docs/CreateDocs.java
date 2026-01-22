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
@Operation(summary = "Create new entity", description = "Creates a new entity with the provided details")
@ApiResponses(value = {
    @ApiResponse(responseCode = "201", description = "Entity created successfully",
        content = @Content(schema = @Schema(implementation = AcademicYearResponseDto.class))),
    @ApiResponse(responseCode = "400", description = "Invalid input or duplicate entity"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
})
public @interface CreateDocs {}
