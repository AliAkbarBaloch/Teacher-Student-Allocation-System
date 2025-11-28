package de.unipassau.allocationsystem.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity representing a teacher form submission.
 * Stores raw web/Excel submissions from teachers for updating their preferences
 * and availability per academic year.
 */
@Entity
@Table(name = "teacher_form_submissions",
        uniqueConstraints = @UniqueConstraint(columnNames = "form_token"),
        indexes = {
                @Index(name = "idx_teacher_form_teacher_id", columnList = "teacher_id"),
                @Index(name = "idx_teacher_form_year_id", columnList = "year_id"),
                @Index(name = "idx_teacher_form_is_processed", columnList = "is_processed")
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeacherFormSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull(message = "Teacher is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    @NotNull(message = "Academic year is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "year_id", nullable = false)
    private AcademicYear academicYear;

    @NotBlank(message = "Form token is required")
    @Column(name = "form_token", nullable = false, unique = true)
    private String formToken;

    @Column(name = "submitted_at", nullable = true, columnDefinition = "TIMESTAMP")
    private LocalDateTime submittedAt;

    // Submission data fields (distinct columns instead of JSON)
    @Column(name = "school_id", nullable = true)
    private Long schoolId;

    @Column(name = "employment_status", length = 50, nullable = true)
    private String employmentStatus;

    @Column(name = "notes", columnDefinition = "TEXT", nullable = true)
    private String notes;

    @Column(name = "subject_ids", columnDefinition = "TEXT", nullable = true)
    private String subjectIds; // Comma-separated list of subject IDs

    @Column(name = "internship_type_preference", length = 50, nullable = true)
    private String internshipTypePreference;

    @Column(name = "internship_combinations", columnDefinition = "TEXT", nullable = true)
    private String internshipCombinations; // Comma-separated list

    @Column(name = "semester_availability", columnDefinition = "TEXT", nullable = true)
    private String semesterAvailability; // Comma-separated list

    @Column(name = "availability_options", columnDefinition = "TEXT", nullable = true)
    private String availabilityOptions; // Comma-separated list

    @NotNull(message = "Processing status is required")
    @Column(name = "is_processed", nullable = false)
    private Boolean isProcessed = false;

    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "TIMESTAMP")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.isProcessed == null) {
            this.isProcessed = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
