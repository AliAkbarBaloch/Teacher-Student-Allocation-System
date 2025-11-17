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
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    @NotNull(message = "Academic year is required")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "year_id", nullable = false)
    private AcademicYear academicYear;

    @NotBlank(message = "Form token is required")
    @Column(name = "form_token", nullable = false, unique = true)
    private String formToken;

    @NotNull(message = "Submission date is required")
    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    @NotBlank(message = "Submission data is required")
    @Column(name = "submission_data", nullable = false, columnDefinition = "TEXT")
    private String submissionData;

    @NotNull(message = "Processing status is required")
    @Column(name = "is_processed", nullable = false)
    private Boolean isProcessed = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
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
