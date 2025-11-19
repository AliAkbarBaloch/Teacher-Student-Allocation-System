package de.unipassau.allocationsystem.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO for creating a new zone constraint.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ZoneConstraintCreateDto {

    @NotNull(message = "Zone number is required")
    @Min(value = 1, message = "Zone number must be positive")
    private Integer zoneNumber;

    @NotNull(message = "Internship type ID is required")
    private Long internshipTypeId;

    @NotNull(message = "Is allowed flag is required")
    private Boolean isAllowed;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
}
