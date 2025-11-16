package de.unipassau.allocationsystem.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entity representing teacher availability for internship assignments.
 * Records per academic year and internship type whether a teacher is available and their preference ranking.
 */
@Entity
@Table(name = "teacher_availability",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_teacher_year_internship",
            columnNames = {"teacher_id", "year_id", "internship_type_id"})
    },
    indexes = {
        @Index(name = "idx_teacher_availability_teacher_id", columnList = "teacher_id"),
        @Index(name = "idx_teacher_availability_year_id", columnList = "year_id"),
        @Index(name = "idx_teacher_availability_internship_type", columnList = "internship_type_id"),
        @Index(name = "idx_teacher_availability_is_available", columnList = "is_available"),
        @Index(name = "idx_teacher_availability_composite", columnList = "teacher_id, year_id")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TeacherAvailability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "availability_id")
    private Long availabilityId;

    @NotNull(message = "Teacher is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    @NotNull(message = "Academic year is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "year_id", nullable = false)
    private AcademicYear academicYear;

    @NotNull(message = "Internship type is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "internship_type_id", nullable = false)
    private InternshipType internshipType;

    @NotNull(message = "Availability status is required")
    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable;

    @Positive(message = "Preference rank must be positive")
    @Column(name = "preference_rank")
    private Integer preferenceRank;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
