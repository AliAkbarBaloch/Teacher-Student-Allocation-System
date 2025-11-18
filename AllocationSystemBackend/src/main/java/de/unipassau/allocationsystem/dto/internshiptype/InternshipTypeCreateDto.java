package de.unipassau.allocationsystem.dto.internshiptype;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InternshipTypeCreateDto {
    @NotBlank(message = "Internship code is required")
    private String internshipCode;

    @NotBlank(message = "Full name is required")
    private String fullName;

    private String timing;
    private String periodType;
    private String semester;
    private Boolean isSubjectSpecific;
    private Integer priorityOrder;
}