package de.unipassau.allocationsystem.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO for updating an existing zone constraint.
 * All fields are optional to support partial updates.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ZoneConstraintUpdateDto {

    @Min(value = 1, message = "Zone number must be positive")
    private Integer zoneNumber;

    private Long internshipTypeId;

    private Boolean isAllowed;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
}
