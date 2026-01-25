package de.unipassau.allocationsystem.dto.internshiptype;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO for updating an existing internship type.
 * All fields are optional for partial updates.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InternshipTypeUpdateDto implements InternshipTypeUpsertDto {
    private String internshipCode;
    private String fullName;
    private String timing;
    private String periodType;
    private Integer semester;
    private Boolean isSubjectSpecific;
    private Integer priorityOrder;
}
