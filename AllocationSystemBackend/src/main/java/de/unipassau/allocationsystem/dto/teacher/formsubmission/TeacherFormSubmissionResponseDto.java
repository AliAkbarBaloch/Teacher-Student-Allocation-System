package de.unipassau.allocationsystem.dto.teacher.formsubmission;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for teacher form submission response.
 * Includes denormalized teacher and academic year information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherFormSubmissionResponseDto {

    private Long id;
    
    // Teacher information
    private Long teacherId;
    private String teacherFirstName;
    private String teacherLastName;
    private String teacherEmail;
    
    // Academic year information
    private Long yearId;
    private String yearName;
    
    // Submission information
    private String formToken;
    private LocalDateTime submittedAt;
    
    // Submission data fields
    private Long schoolId;
    private String employmentStatus;
    private String notes;
    private java.util.List<Long> subjectIds;
    private String internshipTypePreference;
    private java.util.List<String> internshipCombinations;
    private java.util.List<String> semesterAvailability;
    private java.util.List<String> availabilityOptions;
    
    private Boolean isProcessed;
    
    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
