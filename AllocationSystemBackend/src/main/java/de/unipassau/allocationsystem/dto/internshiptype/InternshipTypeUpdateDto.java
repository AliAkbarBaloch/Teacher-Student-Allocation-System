package de.unipassau.allocationsystem.dto.internshiptype;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InternshipTypeUpdateDto {
    private String internshipCode;
    private String fullName;
    private String timing;
    private String periodType;
    private String semester;
    private Boolean isSubjectSpecific;
    private Integer priorityOrder;
}
