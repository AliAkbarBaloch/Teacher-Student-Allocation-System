package de.unipassau.allocationsystem.dto.zoneconstraint;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating a zone constraint.
 * All fields are optional to support partial updates.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ZoneConstraintUpdateDto implements ZoneConstraintUpsertDto {

    @Min(value = 1, message = "Zone number must be positive")
    private Integer zoneNumber;

    private Long internshipTypeId;

    private Boolean isAllowed;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
}
