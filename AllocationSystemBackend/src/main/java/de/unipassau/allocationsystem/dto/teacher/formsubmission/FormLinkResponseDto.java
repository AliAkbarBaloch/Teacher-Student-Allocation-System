package de.unipassau.allocationsystem.dto.teacher.formsubmission;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for form link generation response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FormLinkResponseDto {
    private String formToken;
    private String formUrl;
    private Long teacherId;
    private String teacherName;
    private String teacherEmail;
    private Long yearId;
    private String yearName;
}


