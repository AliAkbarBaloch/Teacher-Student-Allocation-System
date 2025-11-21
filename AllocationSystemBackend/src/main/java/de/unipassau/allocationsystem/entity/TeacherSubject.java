package de.unipassau.allocationsystem.entity;

import jakarta.persistence.*;
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

    public enum AvailabilityStatus {
        AVAILABLE,
        NOT_AVAILABLE,
        LIMITED,
        PREFERRED
    }
}
