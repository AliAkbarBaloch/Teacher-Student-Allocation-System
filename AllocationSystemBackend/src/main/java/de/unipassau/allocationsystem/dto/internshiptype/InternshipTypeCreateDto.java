package de.unipassau.allocationsystem.dto.internshiptype;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a new internship type.
 * Defines the characteristics and properties of an internship category.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InternshipTypeCreateDto implements InternshipTypeUpsertDto {
    @NotBlank(message = "Internship code is required")
    private String internshipCode;

    @NotBlank(message = "Full name is required")
    private String fullName;

    private String timing;
    private String periodType;
    private Integer semester;
    private Boolean isSubjectSpecific;
    private Integer priorityOrder;
}