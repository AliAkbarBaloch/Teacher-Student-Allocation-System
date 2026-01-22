package de.unipassau.allocationsystem.dto.zoneconstraint;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a new zone constraint.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ZoneConstraintCreateDto implements ZoneConstraintUpsertDto {

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
