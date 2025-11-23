package de.unipassau.allocationsystem.dto.teacherassignment;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TeacherAssignmentUpdateDto {

    private Integer studentGroupSize;

    private String assignmentStatus;

    private Boolean isManualOverride;

    private String notes;
}
