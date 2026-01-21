package de.unipassau.allocationsystem.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "teacher_subjects", indexes = {
        @Index(name = "idx_teacher_subject_teacher", columnList = "teacher_id"),
        @Index(name = "idx_teacher_subject_year", columnList = "year_id"),
        @Index(name = "idx_teacher_subject_subject", columnList = "subject_id"),
        @Index(name = "idx_teacher_subject_availability", columnList = "availability_status")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_teacher_subject_year_teacher_subject", columnNames = {"year_id", "teacher_id", "subject_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
/**
 * Entity representing teacher-subject associations per academic year.
 * Tracks which subjects a teacher can teach, availability status, and grade level ranges.
 */
public class TeacherSubject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "year_id", nullable = false, foreignKey = @ForeignKey(name = "fk_teacher_subject_year"))
    private AcademicYear academicYear;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false, foreignKey = @ForeignKey(name = "fk_teacher_subject_teacher"))
    private Teacher teacher;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false, foreignKey = @ForeignKey(name = "fk_teacher_subject_subject"))
    private Subject subject;

    @NotNull
    @Column(name = "availability_status", nullable = false, length = 50)
    private String availabilityStatus;

    @Column(name = "grade_level_from")
    private Integer gradeLevelFrom;

    @Column(name = "grade_level_to")
    private Integer gradeLevelTo;

    @Column(name = "notes", length = 1000)
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "TIMESTAMP")
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

    /**
     * Enumeration of availability statuses for teacher-subject associations.
     */
    public enum AvailabilityStatus {
        AVAILABLE,
        NOT_AVAILABLE,
        LIMITED,
        PREFERRED
    }
}
