package de.unipassau.allocationsystem.dto.internshiptype;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * DTO for internship type response data.
 * Includes all internship type details with timestamps.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InternshipTypeResponseDto {
    private Long id;
    private String internshipCode;
    private String fullName;
    private String timing;
    private String periodType;
    private Integer semester;
    private Boolean isSubjectSpecific;
    private Integer priorityOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}