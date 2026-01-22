package de.unipassau.allocationsystem.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Operation(summary = "Get sort fields", description = "Retrieves available fields that can be used for sorting")
@ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Sort fields retrieved successfully"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
})
public @interface GetSortFieldsDocs {}
